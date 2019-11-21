
import io.grpc.ManagedChannelBuilder
import com.cloriko.protocol.hello.{HelloRequest, GreeterGrpc, HelloReply}
import monix.eval.Callback
val host = "localhost"
val port = 50051
val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build
val request = HelloRequest(name = "World")

val blockingStub = GreeterGrpc.blockingStub(channel)
val reply: HelloReply = blockingStub.sayHello(request)
println(reply)

