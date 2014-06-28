package burp;
import java.io.PrintWriter; 
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import java.net.URLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


//TODO: config file / editable in BURP
//TODO: verification of presence of signature v2 before tampering request
public class BurpExtender implements IBurpExtender, IHttpListener
{
    private String AK;
    private String SK;
    private String ENDPOINT;
    
    private PrintWriter stdout;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        stdout = new PrintWriter(callbacks.getStdout(), true);
        callbacks.setExtensionName("EC2-Resign");
        callbacks.registerHttpListener(this);
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo)
    {
        Properties props = new Properties();
        try {
            FileInputStream fis = new FileInputStream("njord.properties");
            props.load(fis);
        }
        catch (FileNotFoundException ex)  
        {
            stdout.println(ex.toString());
            return;
        }
        catch (IOException ex)  
        {
            return;
        }
        AK = props.getProperty("AK");
        SK = props.getProperty("SK");
        ENDPOINT = props.getProperty("ENDPOINT");
        
        if (messageIsRequest)
        {
            byte[] req = messageInfo.getRequest();
            stdout.println("-------------");
            stdout.println("Original query");
            stdout.println("-------------");
            stdout.println(new String(req));
            
            String data = new String(req).split("\\r?\\n\\r?\\n")[1];
            StringBuilder sbd = new StringBuilder();
            for (int i=0; i<data.split("&").length; i++)
            {
                if (data.split("&")[i].startsWith("Signature=") == false)
                {
                    if (i >0) sbd.append("&");
                    sbd.append(data.split("&")[i]);
                }
            }
            String uri = new String(req).split("\\r?\\n")[0].split(" ")[1];
            String method = new String(req).split("\\r?\\n")[0].split(" ")[0];
            StringBuilder sb2 = new StringBuilder();
            sb2.append(method);
            sb2.append("\n");
            sb2.append(ENDPOINT);
            sb2.append("\n");
            sb2.append(uri);
            sb2.append("\n");
            sb2.append(sbd.toString());
            stdout.println(sb2.toString());
            
            try {
                Mac sha256_HMAC = Mac.getInstance("HmacSHA1");
                SecretKeySpec secret_key = new SecretKeySpec(SK.getBytes("UTF8"), "HmacSHA1");
                sha256_HMAC.init(secret_key);
                String newSign = URLEncoder.encode(new String(Base64.encodeBase64(sha256_HMAC.doFinal(sb2.toString().getBytes("UTF8")))),"UTF8");
                StringBuilder newReq = new StringBuilder();
                newReq.append(new String(req).split("\\r?\\n\\r?\\n")[0]);
                newReq.append("\n\n");
                newReq.append(sbd.toString());
                newReq.append("&Signature=");
                newReq.append(newSign);
                stdout.println("==============");
                stdout.println("Modified query");
                stdout.println("==============");
                stdout.println(newReq.toString());
                messageInfo.setRequest(newReq.toString().getBytes());
            }
            catch (Exception e) {
                stdout.println("Exception catched");
                stdout.println(e.toString());
            }
            stdout.println("End of request");
        }
    }
    
}