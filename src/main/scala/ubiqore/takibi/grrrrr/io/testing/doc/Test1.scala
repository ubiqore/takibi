package ubiqore.takibi.grrrrr.io.testing.doc

import cats.effect.{IO,Resource}
//import cats.implicits._
import java.io._

object CatsEffectTutorial {


def copy(origin: File, destination: File): IO[Long] =
  inputOutputStreams(origin, destination).use { case (in, out) =>
    transfer(in, out)
  }

  // transfer will do the real work
  def transfer(origin: InputStream, destination: OutputStream): IO[Long] = ???

  def inputStream2 (file:File): Resource[IO,FileInputStream] = {
     Resource.make(IO(new FileInputStream(file)))(fip=>IO(fip.close()).handleErrorWith(_=>IO.unit))

  }
  def inputStream(f: File): Resource[IO, FileInputStream] =

    Resource.make {
      IO(new FileInputStream(f))                         // build
    }
      { inStream =>
      IO(inStream.close()).handleErrorWith(_ => IO.unit) // release
      }

  def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.make {
      IO(new FileOutputStream(f))                         // build
    } { outStream =>
      IO(outStream.close()).handleErrorWith(_ => IO.unit) // release
    }

  def inputOutputStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
    for {
      inStream  <- inputStream(in)
      outStream <- outputStream(out)
    } yield (inStream, outStream)
}