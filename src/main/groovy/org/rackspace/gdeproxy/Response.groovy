package org.rackspace.gdeproxy
//class Response:
//    """A simple HTTP Response, with status code, status message, headers, and
//body."""

class Response {

    String code
    String message
    HeaderCollection headers
    String body

    //    def __init__(self, code, message=None, headers=None, body=None):
  //        """
  //Parameters:
  //
  //code - A numerical status code. This doesn't have to be a valid HTTP
  //status code; 600+ values are acceptable also.
  //message - An optional message to go along with the status code. If
  //None, a suitable default will be provided based on the given status
  //.code If ``code`` is not a valid HTTP status code, then the default
  //is the empty string.
  //headers - An optional collection of name/value pairs, either a mapping
  //object like ``dict``, or a HeaderCollection. Defaults to an empty
  //collection.
  //body - An optional response body. Defaults to the empty string.
  //"""
  public Response(code, message=null, headers=null, body=null){

    code = code.toString()
    //
    //        if message is None:
    //            if code in message_by_response_code:
    //                message = message_by_response_code[code]
    //            elif int(code) in message_by_response_code:
    //                message = message_by_response_code[int(code)]
    //            else:
    //                message = ''
    if (message == null) {
      if (HttpResponseMessage.messagesByResponseCode.containsKey(code)){
        message = HttpResponseMessage.messagesByResponseCode[code]
      } else {
        message = ""
      }
    }
    //
    //        if headers is None:
    //            headers = {}
    if (headers == null) {
      headers = HeaderCollection.newInstance()
    }
    //
    //        if body is None:
    //            body = ''
    if (body == null) {
      body = ""
    }
    //
    //        self.code = str(code)
    //        self.message = str(message)
    //        self.headers = HeaderCollection(headers)
    //        self.body = str(body)
    this.code = code
    this.message = message.toString()
    this.headers = new HeaderCollection(headers)
    this.body = ( body ? body.toString() : "" )
  }

  @Override
  String toString() {
    sprintf('Response(code=%s, message=%s, headers=%s, body=%s)', code, message, headers, body)
  }
}



