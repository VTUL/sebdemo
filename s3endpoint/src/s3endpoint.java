// Import required java libraries
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.util.logging.SimpleFormatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;



// Extend HttpServlet class
public class s3endpoint extends HttpServlet {
 
  private static final long serialVersionUID = 1L;
    //private static SimpleFormatter textFormatter;

  public s3endpoint() {
  	super();
  }

  public void init() throws ServletException
  {
      // Do required initialization
 //     	System.out.println("Log4JInitServlet is initializing log4j");
	
  }

  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
      // Set response content type
      response.setContentType("text/html");

      // Actual logic goes here.
      PrintWriter out = response.getWriter();

     	out.println("<html><title>Hello endpoint!</title>" +
    	       "<body bgcolor=FFFFFF>");

    	out.println("<h2>myendpoint</h2>");

    	out.println("<p>Hi! I'm alive. Thanks for asking.</p><p>Returned from doGet</p></body></html>");
    	out.close();
            //System.out.println("GET: " + request.getRequestURI() );
    	System.out.println("GET: " + request.getRequestURI() );

   }
  
  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SecurityException{
    //Get the message type header.
    String messagetype = request.getHeader("x-amz-sns-message-type");
    messagetype = messagetype.trim();
    //If message doesn't have the message type header, don't process it.
    if (messagetype == null)
      return;

    System.out.println(">>messagetype:" + messagetype);
    //Here are some other Amazon SNS specific headers that can 
    //help you determine how to handle the message without 
    //parsing the message body:
    //x-amz-sns-message-id is the message id that you can use 
    //to check if you have already processed this message.
    //x-amz-sns-topic-arn is the ARN of the topic that sent the message. 
    //You can use topic ARN to make sure you are processing messages from the right topic.
    //x-amz-sns-subscription-arn is the ARN for the subscription that topic sent the message to.

    // Parse the JSON message in the message body
    // and hydrate a Message object with its contents 
    // so that we have easy access to the name/value pairs 
    // from the JSON message.
    Scanner scan = new Scanner(request.getInputStream());
    StringBuilder builder = new StringBuilder();
    while (scan.hasNextLine()) {
        builder.append(scan.nextLine());
    }

    System.out.println(">>messagebody: " + builder.toString().trim());
    System.out.println(">>Read Message JSON Begin");

    try {

      Message msg = readMessageFromJson(builder.toString());

      // The signature is based on SignatureVersion 1. 
      // If the sig version is something other than 1, 
      // throw an exception.
      System.out.println(">>Signature Message Begin");
      
      if (msg.getSignatureVersion().equals("1"))
      {
        // Check the signature and throw an exception if the signature verification fails.
        if (isMessageSignatureValid(msg))
          System.out.println(">>Signature verification succeeded");
        else
        {
          System.out.println(">>Signature verification failed");
          throw new SecurityException("Signature verification failed.");
        }
      }
      else
      {
        System.out.println(">>Unexpected signature version. Unable to verify signature.");
            throw new SecurityException("Unexpected signature version. Unable to verify signature.");
      }

          // Process the message based on type.
      System.out.println(">>Notification Message Begin");
      if (messagetype.equals("Notification"))
      {
        
        System.out.println(">>Notification Message");
        String logMsgAndSubject = ">>Notification received from topic " + msg.getTopicArn();
        if (msg.getSubject() != null)
          logMsgAndSubject += " Subject: " + msg.getSubject();
        logMsgAndSubject += " Message: " + msg.getMessage();
        System.out.println(logMsgAndSubject);

        String record = msg.getMessage().toString();
        Container container = readRecordFromJson(record);
        readFieldFromJson(record, container);
        
        // get object string
        String objectstring = record.substring(record.indexOf("\"object\":{"));
        objectstring = "{"+objectstring.substring(0,objectstring.length()-3);
        readObjectFromJson(objectstring, container);
        
        // System.out.println(container.get_bucketName());
        // System.out.println(container.get_eventName());
        // System.out.println(container.get_eventTime());
        // System.out.println(container.get_objectKey());
        // System.out.println(container.get_objectSize());
        // System.out.println(container.get_objectEtag());

        //Create Fedora Container 
        String stringUrl = "http://54.164.25.255:8080/fcrepo4/rest/";

        URL url = new URL(stringUrl);

        StringBuilder postData = new StringBuilder();
	      String s3url = "https://s3.amazonaws.com/"+container.get_bucketName()+"/"+container.get_objectKey();
        
	      postData.append("PREFIX dc: <http://purl.org/dc/elements/1.1/> <> dc:title \""+container.get_objectKey()+"\" . <> dc:source \""+s3url+"\"");
        
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/turtle");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        for (int c; (c = in.read()) >= 0; System.out.print((char)c));
       
      }
      else if (messagetype.equals("SubscriptionConfirmation"))
      {
            //TODO: You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics 
            //that you want to enable to add this endpoint as a subscription.
            
            //Confirm the subscription by going to the subscribeURL location 
            //and capture the return value (XML message body as a string)
              Scanner sc = new Scanner(new URL(msg.getSubscribeURL()).openStream());
              StringBuilder sb = new StringBuilder();
              while (sc.hasNextLine()) {
                  sb.append(sc.nextLine());
              }
              System.out.println(">>Subscription confirmation (" + msg.getSubscribeURL() +") Return value: " + sb.toString());
              //TODO: Process the return value to ensure the endpoint is subscribed.
      }
      else if (messagetype.equals("UnsubscribeConfirmation"))
      {
            //TODO: Handle UnsubscribeConfirmation message. 
            //For example, take action if unsubscribing should not have occurred.
            //You can read the SubscribeURL from this message and 
            //re-subscribe the endpoint.
              System.out.println(">>Unsubscribe confirmation: " + msg.getMessage());
      }
      else
      {
        //TODO: Handle unknown message type.
          System.out.println(">>Unknown message type.");
      }

	   System.out.println(">>Done processing message: " + msg.getMessageId());
    
    } catch (IOException e) {
      e.printStackTrace(System.out);   
    } 


  }
  
  private static Message readMessageFromJson(String json) throws IOException
  {
	System.out.println("readMessageFromJson");
    Message m = new Message();

    JsonFactory f = new JsonFactory();
    JsonParser jp = f.createParser(json);

    jp.nextToken();
    while (jp.nextToken() != JsonToken.END_OBJECT)
    {
      String name = jp.getCurrentName();
      jp.nextToken();
      if ("Type".equals(name))
        m.setType(jp.getText());
      else if ("Message".equals(name))
        m.setMessage(jp.getText());
      else if ("MessageId".equals(name))
        m.setMessageId(jp.getText());
      else if ("SubscribeURL".equals(name))
        m.setSubscribeURL(jp.getText());
      else if ("UnsubscribeURL".equals(name))
        m.setUnsubscribeURL(jp.getText());
      else if ("Subject".equals(name))
        m.setSubject(jp.getText());
      else if ("Timestamp".equals(name))
        m.setTimestamp(jp.getText());
      else if ("TopicArn".equals(name))
        m.setTopicArn(jp.getText());
      else if ("Token".equals(name))
        m.setToken(jp.getText());
      else if ("Signature".equals(name))
        m.setSignature(jp.getText());
      else if ("SignatureVersion".equals(name))
        m.setSignatureVersion(jp.getText());
      else if ("SigningCertURL".equals(name))
        m.setSigningCertURL(jp.getText());
	
    }
	System.out.println("readMessageFromJson-End");
    return m;
  }
  
  private static boolean isMessageSignatureValid(Message msg) {
        try {
                URL url = new URL(msg.getSigningCertURL());
                InputStream inStream = url.openStream();
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
                inStream.close();

                Signature sig = Signature.getInstance("SHA1withRSA");
                sig.initVerify(cert.getPublicKey());
                sig.update(getMessageBytesToSign(msg));
                return sig.verify(Base64.decodeBase64(msg.getSignature()));
        }
        catch (Exception e) {
            throw new SecurityException("Verify method failed.", e);
        }
   }

  
  private static byte [] getMessageBytesToSign (Message msg) {
    byte [] bytesToSign = null;
    if (msg.getType().equals("Notification"))
      bytesToSign = buildNotificationStringToSign(msg).getBytes();
    else if (msg.getType().equals("SubscriptionConfirmation") || msg.getType().equals("UnsubscribeConfirmation"))
      bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
    return bytesToSign;
  }
  
   //Build the string to sign for Notification messages.
   public static String buildNotificationStringToSign( Message msg) {
     String stringToSign = null;
     
     //Build the string to sign from the values in the message.
     //Name and values separated by newline characters
     //The name value pairs are sorted by name 
     //in byte sort order.
     stringToSign = "Message\n";
     stringToSign += msg.getMessage() + "\n";
     stringToSign += "MessageId\n";
     stringToSign += msg.getMessageId() + "\n";
     if (msg.getSubject() != null) {
       stringToSign += "Subject\n";
       stringToSign += msg.getSubject() + "\n";
     }
     stringToSign += "Timestamp\n";
     stringToSign += msg.getTimestamp() + "\n";
     stringToSign += "TopicArn\n";
     stringToSign += msg.getTopicArn() + "\n";
     stringToSign += "Type\n";
     stringToSign += msg.getType() + "\n";
     return stringToSign;
   }

   //Build the string to sign for SubscriptionConfirmation 
   //and UnsubscribeConfirmation messages.
   public static String buildSubscriptionStringToSign(Message msg) {
     String stringToSign = null;
     //Build the string to sign from the values in the message.
     //Name and values separated by newline characters
     //The name value pairs are sorted by name 
     //in byte sort order.
     stringToSign = "Message\n";
     stringToSign += msg.getMessage() + "\n";
     stringToSign += "MessageId\n";
     stringToSign += msg.getMessageId() + "\n";
     stringToSign += "SubscribeURL\n";
     stringToSign += msg.getSubscribeURL() + "\n";
     stringToSign += "Timestamp\n";
     stringToSign += msg.getTimestamp() + "\n";
     stringToSign += "Token\n";
     stringToSign += msg.getToken() + "\n";
     stringToSign += "TopicArn\n";
     stringToSign += msg.getTopicArn() + "\n";
     stringToSign += "Type\n";
     stringToSign += msg.getType() + "\n";
     return stringToSign;
   }


  private static String readFieldFromJson(String json, String field) throws IOException  {
    
    String fieldvalue = "";
    
    JsonFactory f1 = new JsonFactory();
    JsonParser jParser = f1.createParser(json); 
    
    jParser.nextToken();
      while (jParser.nextToken() != JsonToken.END_OBJECT)
      {
        String name = jParser.getCurrentName();
        jParser.nextToken();
        if (field.equals(name))
            fieldvalue = jParser.getText();
      }
  
      jParser.close();
    
      return fieldvalue;
    
  }
  
  private static Container readFieldFromJson(String json, Container c) throws IOException  {
    
    JsonFactory f1 = new JsonFactory();
    JsonParser jParser = f1.createParser(json); 
    
    jParser.nextToken();
      while (jParser.nextToken() != JsonToken.END_OBJECT)
      {
        String name = jParser.getCurrentName();
        
        if ("eventTime".equals(name))
            c.set_eventTime( jParser.getText() );
        else if ("eventName".equals(name))
            c.set_eventName( jParser.getText() );
        
        jParser.nextToken();
      }
  
      jParser.close();
    
      return c;
    
  } 
  
  private static Container readObjectFromJson(String json, Container c) throws IOException  {
    
    JsonFactory f1 = new JsonFactory();
    JsonParser jParser = f1.createParser(json); 
    
    jParser.nextToken();
      while (jParser.nextToken() != JsonToken.END_OBJECT)
      {
        String name = jParser.getCurrentName();

        jParser.nextToken();
        if ("key".equals(name))
            c.set_objectKey( jParser.getText() );
        else if ("size".equals(name))
            c.set_objectSize( jParser.getText() );
        else if ("eTag".equals(name))
            c.set_objectEtag(jParser.getText() );
        
      }
  
      jParser.close();
    
      return c;
    
  }
  
  //get bucket name
  private static Container readRecordFromJson(String record) throws IOException {
    
    Container c = new Container();
    
    String recordstring = record.substring(record.indexOf("\"s3\":{"));
    recordstring = "{"+recordstring.substring(0,recordstring.length()-2);
    c.set_bucketName( readFieldFromJson(recordstring,"name") );
    
    return c;
    
  }

  public void destroy()
  {
      // do nothing.
  }
}
