If you want to add a sensor type, follow these steps:

- (Create your node, if you want)
- Add the type in RegisteredSensor.SensorType
- In the switch inside the handleRegistrationSensor() function of the CoapRemoteDirectoryResource class, add the type
  specifying the name of the subtype the sensor uses to announce itself.
  
If you want to add an actuator type, follow these steps:

- (Create your node, if you want)
- Add the type in RegisteredSensor.SensorType
- If the present actions do not fit your new sensor, add an enum implementing IActuatorAction in the RegisteredActuator class.
- In the switch inside the handleRegistrationSensor() function of the CoapRemoteDirectoryResource class, add the type
  specifying the name of the subtype the sensor uses to announce itself.
- In the switch inside the commandSet() function CommandInterpreter class, add the new type, 
  specifying the Action which are used for your new type
  
  
  
