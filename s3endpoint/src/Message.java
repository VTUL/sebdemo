/*
 * Copyright 2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

public class Message {
	
	
	private String _type;
	private String _messageId;
	private String _message;
	private String _subscribeURL;
	private String _subject;
	private String _timestamp;
	private String _topicArn;
	private String _token;
	private String _signature;
	private String _signatureVersion;
	private String _signingCertURL;
	private String _unsubscribeURL;

	public String getType() {return _type;}
	public void setType(String s) {_type = s;}
	public String getMessageId() {return _messageId;}
	public void setMessageId(String s) {_messageId = s;}
	public String getMessage() {return _message;}
	public void setMessage(String s) {_message = s;}
	public String getSubscribeURL() {return _subscribeURL;}
	public void setSubscribeURL(String s) {_subscribeURL = s;}
	public String getSubject() {return _subject;}
	public void setSubject(String s) {_subject = s;}
	public String getTimestamp() {return _timestamp;}
	public void setTimestamp(String s) {_timestamp = s;}
	public String getTopicArn() {return _topicArn;}
	public void setTopicArn(String s) {_topicArn = s;}
	public String getToken() {return _token;}
	public void setToken(String s) {_token = s;}
	public String getSignature() {return _signature;}
	public void setSignature(String s) {_signature = s;}
	public String getSignatureVersion() {return _signatureVersion;}
	public void setSignatureVersion(String s) {_signatureVersion = s;}
	public String getSigningCertURL() {return _signingCertURL;}
	public void setSigningCertURL(String s) {_signingCertURL = s;}
	public String getUnsubscribeURL() {return _unsubscribeURL;}
	public void setUnsubscribeURL(String s) {_unsubscribeURL = s;}
}
