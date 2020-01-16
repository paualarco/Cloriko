# Cloriko

## Introducction
 
Cloriko is a non yet implemented platform that will provide data storage solution that differs from the cloud model 
in which monthly payments are required and data is stored on the external companies' infrastructure. 
In contrast, using this new model the user will need to use commodity machines as storage service, 
in which they will be able to make it accessible through the Cloriko's platform, from any other authenticated device, 
therefore meaning that the ownership and responsability of the data will exclusively be on the user. 

## Getting started
The below steps shows the process for the user to get started:
-	Create a user account to Cloriko’s website: [www.cloriko.com](https://www.cloriko.com)
-	Installing its software in a machine in which it is wanted to use as storage system.
-   Once installed, there will be accessible the following link: [http://localhost:8980](http://localhost:8980) in which from ther, would be possible to authenticate the machine as a user's `slave` (named to referentiate a user's authenticated device) and also tweak some configurations.
-   At this point, the user does have read and write access the web app to the `slave` documents of a restricted folder. 
-   From there on, the process of adding `slaves` can be repeated as much times as machines the user wants to keep connected to cloriko. 

## Technical overview
The application mainly developed using the Scala language and sbt, it is composed by the 
 following sbt submodules `master`, `slave`, `common` and `frontend`.

### Master
The master is the backend of cloriko's service that conforms the middleware between user requests and slave responses, implemented as a mix of HTTP and gRPC protocols, relatively used for communicating with users and slaves.

- HTTP: The internet application protocol most commonly used for interacting with 
user requests. It is built on top of [Http4s](https://www.http4s.io), which implements all the necessary routes for 
 creating, deleting, moving and fetching from the slaves FS. Moreover, it also provides the 
 routes for fetching the html pages. 

- gRPC: The protocol created by google that is implemented using Remote Procedure Calls, which allow 
to create permanent connections between two end-points. Is because of that feature that this protocol was chosen to implement 
the comunication between the `Master` and the `Slave`. Being the plugin [GrpcMonix](https://www.github.com/beyonthelines/grpcMonix) used to sync with the 
rest of the project that is built on top of [Monix](https://www.monix.io). The master does implements the grpc protocol `MasterSlaveGrpcProtocol` in which it 
can be accessed from the outside world, in this case from the slaves. (The protocol is explained below at the [Common](Common) section).

### Slave
A slave is the representation of a user's machine with the `cloriko-slave` application 
installed on it and authenticated against the `cloriko-master` server.
It does act as an end-point that initializes a permanent grpc connection with the master. At the end, the grpc protocol is a definition of a stream of messages,
in which each message represents specific operation that will be performed on the slave's `File System`. 
This component does also depends on Monix to interact with the master `GrpcMonix` stub and to perform the IO ops. 
It also uses Http4s, in that case to define a local end-point accessible for the user to entry their credentials.

### Frontend 
The front will allow the users to basically authenticate to the server and interact with the data in their Slaves as if they were accessing it locally.

Since the all of the platform is written in the Scala language, the frontend could not be less, so it was develop using the [Play Framework](https://www.playframework.io) that mixes the Scala HTML `scala.html` files, with some of `.js` and `.css`.

### Common
Those functionalities that were required from both `master` and `slave` were included in the `cloriko-common` as a different sub-module.
This is mainly the place where the grpc `.proto` files are defined. These can be found in the `/protobuf` folder, as a standard that allows the the scala protobuf pluguin to found and build them.
It also includes helpful classes to work with the syntax generated from the proto files and a set of generators defined using [scalacheck](https://www.scalacheck.org) which will be needed for testing purposes. 

### End-to-end 
Once we have defined all the c
The application allow the users to create permanent connections between the device and the cloriko server, in which this connection would be accessible for the user 
to store information from anywhere. 
integrates a data transport protocol based on grpc and http that allows the users to create permanent connection from their devices to the cloriko server that makes  their  their devices visible

This section includes the main features of the product: 
Make the desired data available from each of the network’s devices and make it accessible from the Web App.
In order to achieve that, different requirements will be needed to be developed: 
-	Web app that will allow users to register, log in, and see their data.
-	Each device is be automatically set up by installing the Cloriko software and logging in it. 
-	A new folder is being created when installing the software therefore any document can be put there and it will be updated to personal cloud.
-	At least one device is be needed to be registered with the possibility of adding multiple devices, thing that would simulate a cloud behaviour.
-	Three different storage modalities defined: Safe and Light and Duplication
	Safe : All the content updated to the application is replicated in at least two replicas. Note that it is only possible to use this mode when there have been set at least two devices for the same user and a maximum of 3 recommended.
	Lighter mode does not care about any replicas. So the device’s data would be lost in the case that one of the gets down.
	Duplication: This mode will also be available for those cases in which we want all the data in each device. (This mode is very heavy and it is not really seen as a cloud behaviour, instead it is more seen like a shared folder in which each user has the full copy of the data, allowing with no network latency and accessing the full content in an offline mode (explained below).
-	The platform will just be available for online access since it only will be available using to be used from the web app.
-	Two privacy modes, Public and Private.
	Public: Allows users to have the documents their selected opened to everyone (including themselves). So at the end, it avoids the need of having to log in for accessing to any content.
	Private: Just the opposite to Public
-	When the data gets deleted, a notification from the device is sent to the Master server whom will handle it in a different way depending on the storage modality currently being used.
	Safe: In a safe mode the user is asked for choosing between forwarding the deletion


## Future work

As it was said in the introduction section, this paradigm is yet in production nor yet totally implemented and tested, therefore different features can be yet to discover and be developed, in which the initial approach was mainly represented on above explanation, there is already some ideas that could be included in future versions of the same: 

-	Allow multiple contexts & clusters for the same user

-	Subscriber model: A user can be able to subscribe to other user’s content, in that case the data updated by the secondary user does not infer the primary one and neither the secondary user device’s will be seen as part of the primary’s device network.

-	Copy, move and rename operations will be available at the client side 

