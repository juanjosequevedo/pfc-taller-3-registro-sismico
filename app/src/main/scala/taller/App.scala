package taller

object App {
  def main(args: Array[String]): Unit = {
    val r = new RegistroSismico()
    //println(r.conteoPorRegion(Datos.registro))

    println("Prueba con 0.6:")
    println(r.paresCercanos(Datos.registro, 0.6))

    println("Prueba con 1.0:")
    println(r.paresCercanos(Datos.registro, 1.0))

    println("Prueba con 0.1:")
    println(r.conteoPorRegion(Datos.registro))
    println(r.porRegion(Datos.registro).values.map(_.length).sum)

  }
}
