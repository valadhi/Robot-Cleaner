# definition of the robot
define obstaclebotsensor sensor 
(
    size [0.01 0.05 0.01]
    range [0 4]
    fov 10
    samples 1
    color_rgba [0 1 0 0.2]
)

define obstaclebot_ranger ranger 
(   
   obstaclesensor ( pose [ -0.4 -0.3 0 180 ] )
   obstaclesensor ( pose [ -0.4  0.0 0 180 ] )
   obstaclesensor ( pose [ -0.4  0.3 0 180 ] )
   obstaclesensor ( pose [  0.4 -0.3 0   0 ] )
   obstaclesensor ( pose [  0.4  0.0 0   0 ] )
   obstaclesensor ( pose [  0.4  0.3 0   0 ] )
)

define obstaclebot_base position (

   # actual size of robot
   size [0.8 0.8 1.8]
   
   # other parameters
   origin [0 0 0 0]
   gui_nose 1
   obstacle_return 1
   ranger_return 0.5
   blob_return 0
   fiducial_return 0
   localization "gps"
   localization_origin [0 0 0 0]

   # choose a differential drive for the robot
   drive "diff"   

   
)

define obstaclebot_with_sensors obstaclebot_base 
(
   obstaclebot_ranger( pose [0 0 0 0] )
)

define obstaclebot obstaclebot_with_sensors
(
   # define the shape of the robot
   block
   (
      points 4
      point[0] [0 0]
      point[1] [0 0.8]
      point[2] [0.8 0.8]
      point[3] [0.8 0]
      z [0 1.8]
   )
)
