#!/bin/env python
import tornado.httpclient 
import tornado.httpserver 
import tornado.ioloop
import tornado.options
import tornado.web
import re
import hashlib
import hmac
import base64
import random
from datetime import datetime
import os
import sys
import logging


class ProxyHandler(tornado.web.RequestHandler):
    @tornado.web.asynchronous
    def get(self, page_id ='/'):
        logging.info( "%s - %s - GET %s - %s" % (datetime.utcnow(), self.request.remote_ip, self.request.query, self.request.full_url()))
        http_client = tornado.httpclient.AsyncHTTPClient()
        #response = http_client.fetch(tornado.httpclient.HTTPRequest(url =  self.request.full_url(), method= "GET"), self._callback) #Quick'n'Dirty
        client = AsyncHTTPClient()
	client.fetch(self.request.full_url(), self._callback)

    @tornado.web.asynchronous
    def post(self, page_id ='/'):
        logging.info( "%s - %s - POST %s %s" % (datetime.utcnow(), self.request.remote_ip, self.request.query.get_arguments(), self))
        http_client = tornado.httpclient.AsyncHTTPClient()
        response = http_client.fetch(tornado.httpclient.HTTPRequest(url =  self.request.full_url(), method= "GET"), self._callback) #Quick'n'Dirty




    def delete(self, page_id ='/'):
        self.set_status(405)
        self.write("Method not allowed\n")
    def put(self, page_id ='/'):
        self.set_status(405)
        self.write("Method not allowed\n")



    def _callback(self, response): 
        if (response.error):
            self.set_status(500)
        if (response.body):
    	    self.write(response.body)
        else:
            self.write("")
        self.finish()


if __name__ == "__main__":
    tornado.options.parse_command_line()
    application = tornado.web.Application([
        (r"/", ProxyHandler),
    ])
    logging.getLogger().setLevel(logging.INFO)
    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(8080)
    tornado.ioloop.IOLoop.instance().start()

