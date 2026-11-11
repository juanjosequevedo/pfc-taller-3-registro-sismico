package taller

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner
import taller.Datos.registro

@RunWith(classOf[JUnitRunner])
class TestPropioRegistroSismico extends AnyFunSuite {

  val s = new RegistroSismico()
  import s._

  val vacio: List[Sismo] = List()
  val sinChoco = List(
    Sismo(Fecha(2025, 3), "Valle", 4.2, 30),
    Sismo(Fecha(2025, 3), "Santander", 5.1, 150),
    Sismo(Fecha(2025, 4), "Valle", 3.8, 25),
    Sismo(Fecha(2025, 5), "Santander", 4.7, 160),
    Sismo(Fecha(2025, 5), "Valle", 5.3, 35))

  val sinValle = List(
    Sismo(Fecha(2025, 3), "Santander", 5.1, 150),
    Sismo(Fecha(2025, 4), "Choco", 6.0, 40),
    Sismo(Fecha(2025, 5), "Santander", 4.7, 160),
    Sismo(Fecha(2025, 6), "Choco", 3.1, 20))

  // Punto 1 -------------------------------------------------------------------

  test("fuertes: Con 4.5 conserva repetidos y orden de aparición") {
    assert(fuertes(registro, 4.5) == List("Santander", "Choco", "Santander","Valle"))
  }

  test("fuertes: Con 3.1 el sismo de 3.1 exacto queda fuera") {
    assert(fuertes(registro, 3.1) == List("Valle", "Santander", "Valle", "Choco", "Santander", "Valle"))
  }

  test("enRegion: Santander y Choco conservan el orden del registro") {
    assert(enRegion(registro, "Santander") == List(5.1, 4.7))
    assert(enRegion(registro, "Choco") == List(6.0, 3.1))
  }

  test("paresCercanos: Con 0.8 entra Valle-Choco pero no Santander-Choco") {
    assert(paresCercanos(registro, 0.8) == List(("Valle", "Santander"), ("Valle", "Choco")))
  }

  test("paresCercanos: Con una diferencia de 0.0 se emparejan regiones con la misma magnitud") {
    val iguales = List(Sismo(Fecha(2025,1),"A", 4.0,10), Sismo(Fecha(2025,1),"B", 4.0,12))
    assert(paresCercanos(iguales, 0.0) == List(("A", "B")))
  }

  test("paresCercanos: La pareja sale en el orden en que aparecen las regiones") {
    val choco1 = List(
      Sismo(Fecha(2025, 1), "Choco", 6.0, 40),
      Sismo(Fecha(2025, 2), "Valle", 5.9, 30))
    assert(paresCercanos(choco1, 0.2) == List(("Choco", "Valle")))
  }

  // Punto 2 -------------------------------------------------------------------

  test("porRegion: El grupo del Choco trae los sismos completos y en orden") {
    assert(porRegion(registro)("Choco") ==
      List(Sismo(Fecha(2025, 4), "Choco", 6.0, 40),
        Sismo(Fecha(2025, 6), "Choco", 3.1, 20)))
  }

  test("porRegion: Las profundidades de Santander salen en orden de registro") {
    assert(porRegion(registro)("Santander").map(_.profundidad) == List(150,160))
  }

  test("conteoPorRegion: Sin el Choco no aparece y los demás se mantienen") {
    assert(conteoPorRegion(sinChoco) == Map("Valle" -> 3, "Santander" -> 2))
    assert(!conteoPorRegion(sinChoco).contains("Choco"))
  }

  test("conteoPorRegion: la suma de los conteos es el tamaño del registro") {
    assert(conteoPorRegion(registro).values.sum == registro.length)
  }

  test("magnitudMaxima: sin el Chocó quedan Valle y Santander") {
    assert(magnitudMaxima(sinChoco) == Map("Valle" -> 5.3, "Santander" -> 5.1))
  }

  // Punto 3 -------------------------------------------------------------------

  test("conteoSeguro: Regiones desconocidas, incluso con otra escritura, dan cero") {
    assert(conteoSeguro(registro)("Narino") == 0)
    assert(conteoSeguro(registro)("valle") == 0)
  }

  test("conteoSeguro: Santander y Chocó responden 2") {
    assert(conteoSeguro(registro)("Santander") == 2)
    assert(conteoSeguro(registro)("Choco") == 2)
  }

  test("maximaDe: Santander y Valle") {
    assert(maximaDe(registro, "Santander") == Some(5.1))
    assert(maximaDe(registro, "Valle") == Some(5.3))
  }

  test("maximaDe: Si la región no está en el registro dado devuelve None") {
    assert(maximaDe(sinChoco, "Choco") == None)
  }

  test("regionMasActiva: Sin el Valle hay empate Santader-Choco y gana Choco") {
    assert(regionMasActiva(sinValle) == Some("Choco"))
  }

  test("regionMasActiva: Empate de tres regiones, gana la primera alfabéticamente") {
    val tres = List(
      Sismo(Fecha(2025, 1), "Huila", 5.0, 20),
      Sismo(Fecha(2025, 1), "Casanare", 5.0, 20),
      Sismo(Fecha(2025, 1), "Vaupés", 5.0, 20))
    assert(regionMasActiva(tres) == Some("Casanare"))
  }

  // Punto 4 -------------------------------------------------------------------

  test("replicas: Las primeras seis de 10.0") {
    assert(replicas(10.0).take(6).toList == List(10.0, 8.0, 6.4, 5.12, 4.1, 3.28))
  }

  test("replicas: De 1.0 se ve el redondeo de dos decimales") {
    assert(replicas(1.0).take(5).toList == List(1.0, 0.8, 0.64, 0.51, 0.41))
  }

  test("replicas: De 0.0 son todas cero y no se cuelga") {
    assert(replicas(0.0).take(3).toList == List(0.0, 0.0, 0.0))
  }

  test("replicasSensibles: De 6.0 con mínimo 3.0 salen cuatro") {
    assert(replicasSensibles(6.0, 3.0) == List(6.0, 4.8, 3.84, 3.07))
  }

  test("replicasSensibles: De 5.0 con mínimo 1.0 salen ocho") {
    assert(replicasSensibles(5.0, 1.0) ==
      List(5.0, 4.0, 3.2, 2.56, 2.05, 1.64, 1.31, 1.05))
  }

  // Punto 5 -------------------------------------------------------------------

  test("informe: Con mínimo 4.5 cada región conserva muy pocas réplicas") {
    assert(informe(registro, 4.5) ==
      Map("Valle" -> Resumen(3, 5.3, 1),
        "Santander" -> Resumen(2, 5.1, 1),
        "Choco" -> Resumen(2, 6.0, 2)))
  }

  test("informe: Con mínimo 5.5 solo el sismo de 6.0 produce réplicas") {
    assert(informe(registro, 5.5) ==
      Map("Valle" -> Resumen(3, 5.3, 0),
        "Santander" -> Resumen(2, 5.1, 0),
        "Choco" -> Resumen(2, 6.0, 1)))
  }

  test("informe: Con mínimo 2.0 todas las regiones esperan cinco réplicas") {
    assert(informe(registro, 2.0) ==
      Map("Valle" -> Resumen(3, 5.3, 5),
        "Santander" -> Resumen(2, 5.1, 5),
        "Choco" -> Resumen(2, 6.0, 5)))
  }

  test("informe: Sin el Chocó el mapa no lo contiene") {
    assert(informe(sinChoco, 3.0) ==
      Map("Valle" -> Resumen(3, 5.3, 3), "Santander" -> Resumen(2, 5.1, 3)))
  }

  test("informe: Un único sismo débil no genera réplicas sensibles") {
    val uno = List(Sismo(Fecha(2025, 1), "Arauca", 2.0, 10))
    assert(informe(uno, 3.0) == Map("Arauca" -> Resumen(1, 2.0, 0)))
  }
}
