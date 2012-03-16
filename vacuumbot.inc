define finesensor sensor 
(
    size [0.01 0.05 0.01]
    range [0 1.5]
    fov 1
    samples 1
    color_rgba [0 1 0 0.2]
)

define obstaclesensor sensor 
(
    size [0.01 0.05 0.01]
    range [0 4]
    fov 10
    samples 1
    color_rgba [0 1 0 0.2]
)

define exploresensor sensor 
(
    size [0.01 0.05 0.01]
    range [0 10]
    fov 1
    samples 1
    color_rgba [0 1 0 0.2]
)


define robot_ranger ranger 
(
   finesensor( pose [ -0.2 -0.2 0 180  ] )
   finesensor( pose [ -0.2  0.0 0 180  ] )
   finesensor( pose [ -0.2  0.2 0 180  ] )

   finesensor( pose [ -0.2 -0.2 0 0  ] )
   finesensor( pose [ -0.2  0.2 0 0  ] )
   
   finesensor( pose [ -0.15 0.1 0 90 ] )
   finesensor( pose [  0.1  0.1 0 90 ] )
   
   finesensor( pose [ -0.15 -0.1 0 270 ] )
   finesensor( pose [  0.1  -0.1 0 270 ] )
   
   obstaclesensor ( pose [ 0.2 0 0 0 ] )
   obstaclesensor ( pose [ 0.1 0.1 0 5 ] )
   obstaclesensor ( pose [ 0.1 -0.1 0 355 ] )
   
   exploresensor ( pose [0 0 0 0] )
   exploresensor ( pose [0 0 0 90])
   exploresensor ( pose [0 0 0 270])
)

# definition of the robot
define robot_base position (

   # actual size of robot
   size [0.4 0.4 0.1]
   
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

define robot_with_sensors robot_base 
(
   robot_ranger( pose [0 0 0 0] )
)

define vacuumbot robot_with_sensors
(
   # define the shape of the robot
   block
   (
      points 4
      point[0] [0 0]
      point[1] [0.05 0.1]
      point[2] [0.05 0.3]
      point[3] [0 0.4]
      z [0 0.075]
   )

   block
   (
      points 5
      point[0] [0.05 0.1]
      point[1] [0.3 0.1]
      point[2] [0.4 0.2]
      point[3] [0.3 0.3]
      point[4] [0.05 0.3]
      z [0 0.2]
   )
)