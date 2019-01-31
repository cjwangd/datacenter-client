package cn.sh.cares.datacenterclient;

import cn.sh.cares.datacenterclient.message.MqMessage;
import cn.sh.cares.datacenterclient.message.MqMessageBody;
import cn.sh.cares.datacenterclient.message.MqMessageHeader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;

@RunWith(SpringRunner.class)
public class DatacenterClientApplicationTests {

    @Test
    public void contextLoads() throws Exception {
        String resp = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n" +
                "<Root>\n" +
                "    <Header>\n" +
                "        <Sender>ni</Sender>\n" +
                "        <SendTime>2019-01-31 15:19:13</SendTime>\n" +
                "        <Receiver>wo</Receiver>\n" +
                "        <MsgType>yu</MsgType>\n" +
                "    </Header>\n" +
                "    <Body>\n" +
                "        <SeqNum>444</SeqNum>\n" +
                "        <DataType>rr</DataType>\n" +
                "        <List>\n" +
                "            <DATA xsi:type=\"odsSimsScsVo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "                <id>0</id>\n" +
                "                <lkId>8885555</lkId>\n" +
                "            </DATA>\n" +
                "        </List>\n" +
                "    </Body>\n" +
                "</Root>";
        JAXBContext context = JAXBContext.newInstance(MqMessage.class, MqMessageHeader.class, MqMessageBody.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        MqMessage mqMessage = (MqMessage) unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(resp.getBytes())));
        System.out.println("");
    }

}

