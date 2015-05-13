#!/usr/bin/python 
import socket
import io
import time
import picamera
import struct
import sys
from threading import Thread

HOST = ''   # Symbolic name meaning all available interfaces
PORT = 20101 # Arbitrary non-privileged port

class PictureThread(Thread):

	def __init__(self, conn, camera):
		Thread.__init__(self)
		self.conn = conn
		self.camera = camera
		
	def run(self):
		try:
			stream = io.BytesIO()
			while True:
				camera.capture(stream, 'jpeg', use_video_port=True)
				# Rewind the stream and send the image data over the wire
				stream.seek(0)
				conn.write(stream.read())
				conn.flush()
				# Reset the stream for the next capture
        			stream.seek(0)
        			stream.truncate()
		finally:
			conn.close()
			print "--- CONNECTION CLOSED ---"

try:
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	print 'Socket created'
		
	#Bind socket to local host and port
	try:
		s.bind((HOST, PORT))
	except socket.error as msg:
		print 'Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1]
		sys.exit()
	print 'Socket bind complete'
	
	#Start listening on socket
	s.listen(5)
	print 'Socket now listening'
	
	with picamera.PiCamera() as camera:
		camera.resolution = (640, 480)
		camera.framerate = 10
		camera.vflip=True
		camera.hflip=True
		#let the camera warm up for 2 seconds
		time.sleep(2)
	
		while 1:
			conn = s.accept()[0].makefile('wb')
			#print "Connected with " + conn[0]
			thread = PictureThread(conn, camera)
			print "starting thread"
			thread.start()
               	 	print "thread started, waiting thread to finish"
               		thread.join()
                	print "thread finish, waiting new connection"
finally:
	s.close()
	print "--- SOCKET CLOSED ---"

