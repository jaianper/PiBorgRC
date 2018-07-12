#!/usr/bin/env python
# coding: Latin-1

# Load library functions we want
import time
import os
import sys
import socket
import PicoBorgRev

# TCP server values
SERVER_HOST = "0.0.0.0"
SERVER_PORT = 12345
interval = .05

# PiCamera stream values
UDP_HOST = "192.168.0.10"  # IP of the mobile device
UDP_PORT = 9000

# Global values
global PBR

# Setup the PicoBorg Reverse
PBR = PicoBorgRev.PicoBorgRev()
#PBR.i2cAddress = 0x44                  # Uncomment and change the value if you have changed the board address
PBR.Init()
if not PBR.foundChip:
    boards = PicoBorgRev.ScanForPicoBorgReverse()
    if len(boards) == 0:
        print 'No PicoBorg Reverse found, check you are attached :)'
    else:
        print 'No PicoBorg Reverse at address %02X, but we did find boards:' % (PBR.i2cAddress)
        for board in boards:
            print '    %02X (%d)' % (board, board)
        print 'If you need to change the I²C address change the setup line so it is correct, e.g.'
        print 'PBR.i2cAddress = 0x%02X' % (boards[0])
    sys.exit()
#PBR.SetEpoIgnore(True)                 # Uncomment to disable EPO latch, needed if you do not have a switch / jumper
PBR.SetCommsFailsafe(False)
PBR.ResetEpo()

# Power settings
voltageIn = 11.1                        # Total battery voltage to the PicoBorg Reverse
voltageOut = 4.5                        # Maximum motor voltage

# Setup the power limits
if voltageOut > voltageIn:
    maxPower = 1.0
else:
    maxPower = voltageOut / float(voltageIn)

# Start sequence
print 'Setup camera'
os.system("raspivid -n -w 320 -h 240 -b 4500000 -fps 10 -vf -hf -t 0 -o - | "+
          "gst-launch-1.0 -e -v fdsrc !  h264parse ! rtph264pay config-interval=10 pt=96 ! udpsink host="+UDP_HOST+" port="+UDP_PORT+"&");

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print 'Socket created'

try:
    s.bind((SERVER_HOST, SERVER_PORT))
    
    s.listen(5)
    print 'Socket awaiting messages'
    (conn, addr) = s.accept()
    print 'Connected'

    # awaiting for message
    while True:
        reqData = conn.recv(1024)
        if reqData.startswith('/'):
            print 'I sent a message back in response to: ' + reqData
        reply = ''

        if reqData.startswith('/off'):
            # Turn the drives off
            PBR.MotorsOff()
        elif reqData.startswith('/set/'):
            # Motor power setting: /set/driveLeft/driveRight
            parts = reqData.split('/')
            # Get the power levels
            if len(parts) >= 4:
                try:
                    driveLeft = float(parts[2])
                    driveRight = float(parts[3])
                except:
                    # Bad values
                    driveRight = 0.0
                    driveLeft = 0.0
            else:
                # Bad request
                driveRight = 0.0
                driveLeft = 0.0
            # Ensure settings are within limits
            if driveRight < -1:
                driveRight = -1
            elif driveRight > 1:
                driveRight = 1
            if driveLeft < -1:
                driveLeft = -1
            elif driveLeft > 1:
                driveLeft = 1
            # Set the outputs
            driveLeft *= maxPower
            driveRight *= maxPower
            PBR.SetMotor1(driveRight)
            PBR.SetMotor2(-driveLeft)
        # Wait for the interval period
        time.sleep(interval)
except socket.error:
    #managing error exception
    print 'Bind failed '
except KeyboardInterrupt:
    # CTRL+C exit
    print '\nUser shutdown'
finally:
    # Turn the motors off under all scenarios
    PBR.MotorsOff()
    print 'Motors off'
    conn.close() # Close connections

os.system("killall raspivid");
PBR.SetLed(True)
print 'Socket Server terminated.'
