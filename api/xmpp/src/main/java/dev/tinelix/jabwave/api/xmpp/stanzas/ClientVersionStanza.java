package dev.tinelix.jabwave.api.xmpp.stanzas;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class ClientVersionStanza extends IQ {
    public final static String childElementName = "query";
    public final static String childResultElementName = "result";
    public final static String childElementNamespace = "jabber:iq:version";
    private String client_name;
    private String client_version;
    private String os_version;
    private boolean doNotShareOSInfo;

    public ClientVersionStanza(String from, String to) throws XmppStringprepException {
        super(childElementName, childElementNamespace);
        setFrom(JidCreate.from(from));
        setTo(JidCreate.from(to));
    }

    public ClientVersionStanza(String to) throws XmppStringprepException {
        super(childElementName, childElementNamespace);
        setTo(JidCreate.from(to));
    }

    public void setClientInfo(String client_name, String client_version, String os_version) {
        this.client_name = client_name;
        this.client_version = client_version;
        if(!doNotShareOSInfo) {
            this.os_version = os_version;
        }
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.openElement("name");
        xml.append(client_name);
        xml.closeElement("name");
        xml.openElement("version");
        xml.append(client_version);
        xml.closeElement("version");
        if(!doNotShareOSInfo) {
            xml.openElement("os");
            xml.append(os_version);
            xml.closeElement("os");
        }
        return xml;
    }
}
