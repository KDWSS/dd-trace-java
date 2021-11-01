package datadog.trace.instrumentation.http4s021_212

import datadog.trace.bootstrap.instrumentation.api.{URIDataAdapter, UTF8BytesString}
import datadog.trace.bootstrap.instrumentation.decorator.HttpClientDecorator
import org.http4s.{Request, Response}

import java.net.URI

object Http4sHttpClientDecorator {
  val HTTP4S_HTTP_REQUEST: UTF8BytesString =
    UTF8BytesString.create("http4s-http.request")
  val HTTP4S_HTTP_CLIENT: UTF8BytesString =
    UTF8BytesString.create("http4s-http-client")
  private val Decorator = new Http4sHttpClientDecorator()

  def decorator[F[_]]: Http4sHttpClientDecorator[F] =
    Decorator.asInstanceOf[Http4sHttpClientDecorator[F]]
}

final class Http4sHttpClientDecorator[F[_]] extends HttpClientDecorator[Request[F], Response[F]] {
  override protected def instrumentationNames: Array[String] =
    Array[String]("http4s")

  override protected def component: CharSequence =
    Http4sHttpClientDecorator.HTTP4S_HTTP_CLIENT

  override protected def method(request: Request[F]): String =
    request.method.name

  override protected def url(request: Request[F]): URI =
    new URI(request.uri.renderString)

  override protected def status(httpResponse: Response[F]): Int =
    httpResponse.status.code
}
