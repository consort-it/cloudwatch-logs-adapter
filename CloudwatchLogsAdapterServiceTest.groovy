package com.consort.service

import com.amazonaws.services.logs.AWSLogs
import com.amazonaws.services.logs.model.DescribeLogGroupsResult
import com.amazonaws.services.logs.model.DescribeLogStreamsResult
import com.amazonaws.services.logs.model.GetLogEventsResult
import com.amazonaws.services.logs.model.LogGroup
import com.amazonaws.services.logs.model.LogStream
import com.amazonaws.services.logs.model.OutputLogEvent
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class disabled extends Specification {

    String groupString = "dev.k8s.consort-it.de"
    String streamString = "friendly_saha-b76e83fc1ef2"
    @Shared logsReader
    @Shared underTest

    def setupSpec() {
        underTest = new CloudWatchLogsService()

        logsReader = Stub(AWSLogs.class)
        logsReader.describeLogGroups(_) >> { _ ->
            DescribeLogGroupsResult res = new DescribeLogGroupsResult()
            List<LogGroup> list = new ArrayList<LogGroup>()
            LogGroup group = new LogGroup()
            group.setArn("test")
            group.setCreationTime(12348)
            group.setLogGroupName("dev.k8s.consort-it.de")
            group.setMetricFilterCount(12345)
            group.setRetentionInDays(12)
            group.setStoredBytes(12)
            list.add(group)
            res.setLogGroups(list)
            res.serialVersionUID = 432018412412
            return res
        }
        logsReader.describeLogStreams(_) >> { _ ->
            DescribeLogStreamsResult res = new DescribeLogStreamsResult()
            List<LogStream> list = new ArrayList<LogStream>()
            LogStream stream = new LogStream()
            stream.setArn("test")
            stream.setCreationTime(12348)
            stream.setLogStreamName("friendly_saha-b76e83fc1ef2")
            stream.setStoredBytes(12345)
            stream.setCreationTime(12)
            stream.setFirstEventTimestamp(12)
            stream.setLastEventTimestamp(12)
            stream.setUploadSequenceToken("12")
            list.add(stream)
            res.setLogStreams(list)
            res.serialVersionUID = 432018412413
            return res
        }
        logsReader.getLogEvents(_) >> { _ ->
            GetLogEventsResult res = new GetLogEventsResult()
            List<OutputLogEvent> list = new ArrayList<OutputLogEvent>()
            OutputLogEvent event = new OutputLogEvent()
            event.setMessage("test")
            event.setTimestamp(12348)
            event.setIngestionTime(12345)
            list.add(event)
            res.setEvents(list)
            res.serialVersionUID = 432018412414
            return res
        }
        underTest.withLogsReader(logsReader)
    }

    def "Check search for group | assuming there are logs in CloudWatch available"() {
        when:
        List<LogGroup> groups = underTest.searchGroups("e")

        then:
        groups.size > 0
        groups[0].getLogGroupName().contains("e")
    }

    def "Check search for stream | assuming there are logs in CloudWatch available"() {
        when:
        List<LogStream> streams = underTest.searchStreams("a")

        then:
        streams.size > 0
        streams[0].getLogStreamName().contains("a")
    }

    def "Check to get all groups | assuming there are groups in CloudWatch available"() {
        when:
        List<LogGroup> groups = underTest.getGroups()

        then:
        groups.size() > 0
    }

    def "Check to get single groups | assuming there is this one group in CloudWatch available"() {
        when:
        LogGroup group = underTest.getGroup(groupString)

        then:
        group.getLogGroupName() == groupString
    }

    def "Check to get all streams of a group | assuming there are streams in CloudWatch available"() {
        when:
        List<LogStream> streams = underTest.getStreamsOfGroup(underTest.getGroup(groupString))

        then:
        streams.size() > 0
    }

    def "Check to get single stream of a group | assuming there is this one stream in CloudWatch available"() {
        when:
        LogStream stream = underTest.getStreamOfGroup(underTest.getGroup(groupString), streamString)

        then:
        stream.getLogStreamName() == streamString
    }

    def "Check to get logs of stream and group | assuming there are logs in CloudWatch available"() {
        when:
        List<OutputLogEvent> logs = underTest.getLogByGroupAndStream(underTest.getGroup(groupString), underTest.getStreamOfGroup(underTest.getGroup(groupString), streamString), 2)

        then:
        logs.size > 0
        logs.size <= 2
    }

    def "Check to get streams | assuming there are streams in CloudWatch available"() {
        when:
        List<LogStream> streams = underTest.getStreams(streamString)

        then:
        streams.size > 0
    }

    def "Check to get logs of stream | assuming there are logs in CloudWatch available"() {
        when:
        List<List<OutputLogEvent>> logs = underTest.getLogsByStream(streamString, 2)

        then:
        logs.size > 0
        logs[0].size > 0
        logs[0].size <= 2
    }
}
