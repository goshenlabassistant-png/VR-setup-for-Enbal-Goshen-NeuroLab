import json
import math
import socket
import bge 
import time
import select

#! check this file!!!
# This script is linked to the player_path object
# This script is in charge of moving the player in the game according to the encoder information send from the java program

SENSITIVITY_PARAMETER = 0.00097

  
player_path = bge.logic.getCurrentScene().objects['player_path']    


# Update the location and IR information in the data table 
# player_path['game_counter']+=1 
# col = player_path['ROTATE_ENCODER_DATA'] 
# row = player_path['game_counter']
# player_path['output_data'][col][row] = player_path['last_position']
# player_path['output_data'][col][row] = player_path['last_position']

def is_good_number(s):
    try:
        num = int(s)
        if num == 0:
            return False
        return True
    except ValueError:
        return False

def apply_movement(): 
  # This function reads the encoder-arduino information and move
  # the player in the game according to it:
  
  sock = player_path['java_socket_obj']
  ready_to_read, _, _ = select.select([sock], [], [], 0)
  java_code_read = None

  if ready_to_read:
    try:
        data = sock.recv(1024)
        if not data:
            print("Connection closed by Java.")
            java_code_read = None
        else:
            java_code_read = data.decode("utf-8")
    except socket.timeout:
        java_code_read = None
    except (ConnectionResetError, OSError) as e:
        print("Socket error:", e)
        java_code_read = None

  if java_code_read:
    split_code_read = java_code_read.split("\n")
    for code in split_code_read:
        #? maybe to sum all numbers?
        if is_good_number(code):
          # delta is the movment progress
          delta = int(code)
          # If the encoder rotates, move the player:
          move_player(delta)

          #TODO make sure it doesnt go one step to foward with it saying it is backwards, because it can ruin the reward collision event
          # Update the 'backwards movment' value if necessary 
          # (counts the backwards movments of the mouse in order to keep him from getting things if he went backwards for them)
          if player_path['backwards_movment'] > 0:
            if delta < 0:
              player_path['backwards_movment'] = delta
          elif player_path['backwards_movment'] <= 0:
            player_path['backwards_movment'] += delta
            # on a positive count (the count doesn't matter, just that it's positive)
            if player_path['backwards_movment'] > 0:
              player_path['backwards_movment'] = 1

          sendDataToJava(delta) # will do it on time? check after
 
  
def move_player(delta):
  # This function gets the deltea move and moves the player in the game:
  # Find out the current coordinates:
  [_,_,current_z] = player_path.worldOrientation.to_euler()
  # Change the z coordinate:
  #! Notice: the z coordinate is the oppesite way, - is forward and + is backwards!! That's why i multiply by -1, to sync it
  player_path.worldOrientation = [0,0,current_z + SENSITIVITY_PARAMETER*delta*-1]


def sendDataToJava(new_position):
  # This function sends parameters to the java program:
  # like: lap number, if got reward, etc.
  #? will it send the right parameters? before or after the updating laps and so on? maybe I need a sleep or something
  #! check that the data is sent after the recalculation of the treat and lap number!!
  # Send the parameters to the java program:

  #? add the time here? or in the java program, before sending?
  #? add maze location?
  data_to_send = {
        "laps": player_path['laps_counter'],
        "reward": player_path['reward_collision'],
        "location": calculateLocationRightWay()
    }
  json_data = json.dumps(data_to_send)
  player_path['reward_collision'] = False
  player_path['java_socket_obj'].send((json_data + '\n').encode('utf-8'))

def calculateLocationRightWay():
   # Z is the oppesite way, so it's no trivial
  radsLocation = player_path.worldOrientation.to_euler().z * -1
  degLocation = math.degrees(radsLocation) % 360
  if degLocation < 0:
    degLocation += 360
  return degLocation


if player_path['sockets_configed']:
    apply_movement()