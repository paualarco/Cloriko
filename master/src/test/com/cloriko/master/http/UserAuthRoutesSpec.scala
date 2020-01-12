package com.cloriko.master.http

import com.cloriko.master.http.UserRoutes.{SignInEntity, SignUpEntity}
import cats.effect.IO
import org.http4s.{Response, Status}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}

class UserAuthRoutesSpec
  extends WordSpecLike
    with Matchers
    with ScalaFutures
    with Generators
    with HttpFixture {

  val genSignInEntity: () => SignInEntity = () => SignInEntity(genUsername(), genString(10))
  val genSignUpEntity: () => SignUpEntity = () => SignUpEntity(genUsername(), genString(10), genString(7), genString(7), genString(10))

  "A single SignUp http request" should {

    "return status code `Created`" when {
      "a new user signs up" in  {
        //given
        val signUpEntity = genSignUpEntity() //SignUpEntity("newUserName", "SecurePassword125!", "Anthon", "Kan", "anthon.kan@gmail.com")

        //when
        val httpResponse: Response[IO] = httpRequest(signUpEntity)

        //then
        httpResponse.status shouldEqual Status.Created
      }
    }

    "return status code `Ok`" when {
      "the user already existed" in {
        //given
        val signUpEntity: SignUpEntity= genSignUpEntity()

        //when
        val firstSignUpResponse: Response[IO]  = httpRequest(signUpEntity)
        val secondSignUpResponse: Response[IO] = httpRequest(signUpEntity)


        //then
        firstSignUpResponse.status  shouldEqual Status.Created
        secondSignUpResponse.status shouldEqual Status.Ok
      }
    }

    "return status code `BadRequest`" when {
      "the username length is lower than 5" in {
        //given
        val signUpEntity = genSignUpEntity().copy(userName = "123")

        //when
        val httpResponse: Response[IO] = httpRequest(signUpEntity)

        //then
        httpResponse.status shouldEqual Status.BadRequest
      }
    }


    "A sign in http request" should {

      "return status code `Not found`" when {
        "the same username has not been registered before" in {
          //given
          val signInEntity: SignInEntity= genSignInEntity()

          //when
          val httpResponse: Response[IO] = httpRequest(signInEntity)

          //then
          httpResponse.status shouldEqual Status.NotFound
        }
      }

      "return status code `Accepted`" when {
        "the credentials given matches with an already existing user" in new Fixture {
          //given
          val signUpEntity: SignUpEntity = genSignUpEntity().copy(userName = username, password = password)
          val signInEntity: SignInEntity = SignInEntity(username, password)

          //when
          val signUpHttpResponse: Response[IO] = httpRequest(signUpEntity)
          val signInHttpResponse: Response[IO] = httpRequest(signInEntity)

          //then
          signUpHttpResponse.status shouldEqual Status.Created
          signInHttpResponse.status shouldEqual Status.Accepted
        }
      }

      "return status code `Forbidden`" when {
        "the credentials passed does not match with any of the existing users" in new Fixture {
          //given
          val signUpEntity: SignUpEntity = genSignUpEntity().copy(userName = username, password = differentPassword)
          val signInEntity: SignInEntity = SignInEntity(username, password)

          //when
          val signUpHttpResponse: Response[IO] = httpRequest(signUpEntity)
          val signInHttpResponse: Response[IO] = httpRequest(signInEntity)

          //then
          signUpHttpResponse.status shouldEqual Status.Created
          signInHttpResponse.status shouldEqual Status.Forbidden
        }
      }
    }

    trait Fixture {
      val username = genString(10)
      val password = genString(10)
      val differentPassword = genString(10)
    }
  }
}





