Para justificar las consultas, primero revisamos qué ocurre con una
lista vacía. Después suponemos que la respuesta para el resto del
registro es correcta y vemos qué pasa al añadir un sismo al frente.

En las réplicas revisamos los primeros valores que se solicitan.
La secuencia completa no tiene un último elemento.

Usamos estas convenciones:

- $[]$ representa una lista vacía.
- $s :: r$ es una lista cuyo primer sismo es $s$ y cuyo resto es $r$.
- $m(s)$ indica la magnitud de un evento.
- $g(s)$ representa su región.
- $|r|$ es la cantidad de elementos de una lista.

Trabajamos con registros finitos y magnitudes finitas. Para las consultas
de réplicas sensibles, el mínimo es mayor o igual a cero, como exige
la comprobación escrita en el código.

## 1. Consultas con `for`

### 1.1. Corrección de `fuertes`

#### Qué debe calcular

Llamamos $F(r,u)$ a la lista de regiones cuyos sismos superan el
umbral $u$, respetando el orden y las repeticiones.

Con una entrada vacía:

$$
F([],u)=[]
$$

Si el primer sismo supera el umbral:

$$
F(s::r,u)=g(s)::F(r,u)
$$

Cuando no lo supera:

$$
F(s::r,u)=F(r,u)
$$

#### Caso base

Si `r` está vacío, el `for` no encuentra elementos y produce `List()`.
Esto coincide con la definición.

#### Paso con un sismo más

Suponemos que la consulta funciona para el resto del registro.

Al añadir un evento al frente, la condición comprueba si su magnitud
es mayor que el umbral. Si cumple, `yield` agrega su región antes de
los resultados del resto. En caso contrario, no aporta ningún nombre.

Así se obtiene exactamente la lista pedida. No se quitan repetidos
ni se cambia el orden.

#### Terminación

El recorrido revisa una lista finita. Cada sismo se considera una vez,
por lo que la consulta termina al llegar al final.

### 1.2. Corrección de `enRegion`

#### Qué debe calcular

Sea $E(r,a)$ la lista de magnitudes registradas en la región $a$.

$$
E([],a)=[]
$$

Si el primer evento pertenece a esa región:

$$
E(s::r,a)=m(s)::E(r,a)
$$

Cuando corresponde a otra:

$$
E(s::r,a)=E(r,a)
$$

#### Caso base

Una lista vacía no contiene eventos de ninguna región. El resultado
`List()` es el esperado.

#### Paso con un sismo más

Suponemos que las magnitudes del resto ya se obtienen correctamente.

El código compara la región del primer evento con la solicitada.
Si son iguales, añade su magnitud. Si son distintas, continúa sin
incluirla.

Los resultados del resto mantienen su posición, así que la respuesta
contiene todas las magnitudes buscadas en el orden original.

#### Terminación

La consulta termina porque el registro tiene una cantidad finita
de elementos.

### 1.3. Corrección de `paresCercanos`

#### Qué debe calcular

Una pareja de regiones diferentes debe aparecer cuando existen dos
sismos, uno de cada región, cuyas magnitudes cumplen:

$$
|m(s_1)-m(s_2)|\leq dif
$$

Cada pareja debe salir una sola vez y sus nombres deben respetar
la primera aparición de las regiones en el registro.

#### Regiones distintas y orden

La lista `regiones` se obtiene quitando nombres repetidos con `distinct`.

Llamamos $pos(a)$ a la posición de la región $a$ en esa lista.
La condición del código exige:

$$
pos(region1)<pos(region2)
$$

Una región no puede estar antes que ella misma. Por eso quedan fuera
las parejas de nombres iguales.

Además, si una pareja cumple esa comparación, la pareja invertida
no puede cumplirla. Así se conserva una sola orientación.

#### Parejas que se incluyen

Antes de producir una respuesta, el código comprueba que cada sismo
pertenece a la región que le corresponde y que la diferencia entre
las magnitudes no supera `dif`.

Por tanto, toda pareja incluida tiene dos eventos que justifican
su presencia.

#### Parejas que deben encontrarse

Los recorridos consideran todas las regiones y todos los sismos.
Si existe una pareja válida, sus eventos se revisan en algún momento
y las condiciones permiten incluirla.

Cuando varias combinaciones producen los mismos nombres, el último
`distinct` conserva únicamente la primera aparición.

Si no hay datos, o solo existe una región, no se obtiene ninguna pareja.

#### Terminación

Las cuatro listas recorridas son finitas. Aunque se comparen varias
combinaciones, la cantidad de revisiones también es finita.

## 2. Agrupación por región

### 2.1. Corrección de `porRegion`

#### Qué debe calcular

Para explicar los grupos, llamamos $G(r,a)$ a la lista de sismos de
la región $a$ dentro de $r$.

Cuando no hay eventos:

$$
G([],a)=[]
$$

Si el primer sismo pertenece a $a$:

$$
G(s::r,a)=s::G(r,a)
$$

Si corresponde a otra región:

$$
G(s::r,a)=G(r,a)
$$

Esta notación permite hablar de una lista vacía para una región ausente.
En el mapa real, esas regiones no tienen una entrada.

#### Caso base

`groupBy` aplicado a una lista vacía produce `Map()`. No crea grupos
sin sismos.

#### Paso con un sismo más

Suponemos que los eventos del resto están en sus grupos correctos.

Al añadir un sismo, `groupBy` usa su región para ubicarlo. El evento
solo pertenece a ese grupo y los demás conservan sus integrantes.

Cada aparición del registro se mantiene, incluso si hay dos sismos
con los mismos datos.

Por eso el resultado reúne todos los eventos por región, sin perderlos
ni enviarlos a un grupo diferente.

### 2.2. Corrección de `conteoPorRegion`

Llamamos $C(r,a)$ a la cantidad de sismos de la región $a$.

$$
C(r,a)=|G(r,a)|
$$

Si no hay eventos de esa región, la cantidad es cero. Al añadir
un sismo suyo, aumenta en uno:

$$
C(s::r,a)=1+C(r,a)\qquad\text{si }g(s)=a
$$

Si el evento pertenece a otra región, el conteo permanece igual:

$$
C(s::r,a)=C(r,a)\qquad\text{si }g(s)\neq a
$$

`porRegion` forma los grupos correctamente. Luego, `length` obtiene
el número de elementos de cada lista, que coincide con $C(r,a)$.

El mapa solo incluye regiones presentes. Con un registro vacío,
la respuesta es `Map()`.

### 2.3. Corrección de `magnitudMaxima`

Para una región presente en el registro, llamamos $M(r,a)$ a su mayor
magnitud:

$$
M(r,a)=\max\{m(s)\mid s\text{ pertenece a }G(r,a)\}
$$

#### Grupo con un sismo

Si solo existe un evento, su magnitud es también la mayor.
La operación `max` devuelve ese valor.

#### Grupo con un elemento más

Suponemos que ya conocemos el máximo de los demás eventos.
Al incluir una nueva magnitud, hay dos posibilidades:

- Si supera el máximo anterior, pasa a ser la mayor.
- Si es menor o igual, se conserva el valor que ya se tenía.

Esto se expresa como:

$$
M(s::r,a)=\max(m(s),M(r,a))
$$

La fórmula corresponde al caso en que $s$ pertenece a $a$ y esa región
ya tenía eventos en $r$. Si es su primer sismo, el máximo es $m(s)$.

El código obtiene las magnitudes del grupo y aplica `max`, que realiza
esa elección.

#### Listas vacías

Cada grupo creado por `porRegion` contiene al menos un evento.
Por eso no se busca el máximo de una lista vacía.

Si el registro completo está vacío, no hay grupos que procesar y
se devuelve `Map()`.

#### Terminación del punto 2

Las tres funciones trabajan con el registro y con grupos finitos.
Agrupar, contar y buscar el máximo terminan después de revisar
sus elementos.

## 3. Consultas seguras

### 3.1. Corrección de `conteoSeguro`

Para una región registrada, la respuesta debe ser su cantidad de
eventos. Si no aparece, debe ser cero.

$$
conteoSeguro(r)(a)=\begin{cases}C(r,a)&\text{si }a\text{ aparece en }r\\0&\text{si }a\text{ no aparece en }r\end{cases}
$$

`conteoPorRegion` ya calcula correctamente los valores existentes.
`withDefaultValue(0)` establece la respuesta para una consulta con
paréntesis a un nombre que no está en el mapa.

No cambia los conteos ni agrega regiones ausentes.

### 3.2. Corrección de `maximaDe`

Para una región con eventos, la consulta debe entregar su magnitud
máxima dentro de `Some`. Cuando no hay datos, debe devolver `None`.

$$
maximaDe(r,a)=\begin{cases}\operatorname{Some}(M(r,a))&\text{si }a\text{ aparece en }r\\\operatorname{None}&\text{si }a\text{ no aparece en }r\end{cases}
$$

La función usa el mapa de máximos, cuya respuesta se justificó en
el punto anterior.

Al aplicar `get`, obtiene `Some` con el valor guardado o `None` si
el nombre está ausente. Ambos casos coinciden con lo solicitado.

### 3.3. Corrección de `regionMasActiva`

#### Registro vacío

Cuando no hay sismos, no existe una región que pueda elegirse.
La condición inicial devuelve `None`.

#### Registro con eventos

La región elegida debe tener el mayor conteo. Si la llamamos $a^*$:

$$
C(r,a^*)=\max\{C(r,a)\mid a\text{ aparece en }r\}
$$

El código ordena por:

$$
(-C(r,a),a)
$$

Si una cantidad es mayor que otra, su valor negativo es menor.
Por eso, al ordenar de menor a mayor, los conteos más altos
quedan primero.

Cuando las cantidades coinciden, se compara el nombre de la región.
Así se resuelve el empate por orden alfabético.

`headOption` toma la primera entrada y `map` conserva su nombre,
dentro de `Some`.

#### Terminación del punto 3

Los conteos y máximos se obtienen de listas finitas. Las búsquedas
terminan y el ordenamiento tiene una cantidad finita de regiones.

## 4. Réplicas

### 4.1. Corrección del redondeo

Llamamos $R(x)$ al cálculo utilizado para redondear:

$$
R(x)=\frac{\operatorname{round}(100x)}{100}
$$

Multiplicar por cien lleva los dos primeros decimales a la parte
entera. `Math.round` elige el entero correspondiente y la división
por `100.0` devuelve el valor a su escala original.

Esta es la misma operación indicada en el enunciado.

### 4.2. Corrección de `replicas`

#### Qué debe producir

Partimos de la magnitud recibida:

$$
m_0=magnitud
$$

Cada paso calcula:

$$
m_{n+1}=0.8\,m_n
$$

El valor que se entrega en la posición $n$ es:

$$
a_n=R(m_n)
$$

El código usa `Double` para estas operaciones. El redondeo se aplica
al valor entregado, mientras que el siguiente cálculo parte de
`magnitud`, como aparece en la llamada:

```scala
replicas(magnitud * 0.8)
```

#### Primer elemento

La función calcula `redondear(magnitud)`, que corresponde a $R(m_0)$.
Por tanto, el primer valor es el esperado.

#### Siguiente elemento

Suponemos que se han obtenido correctamente los primeros valores.

Al pedir otro elemento, la parte pendiente llama a `replicas` con
la magnitud anterior multiplicada por `0.8`. Esa llamada redondea
el nuevo dato y deja preparado el siguiente paso.

De esta manera, cada posición cumple la regla indicada.

#### Cantidad finita de valores

Para comprobar esta función se considera una cantidad finita $n$:

$$
replicas(magnitud).take(n).toList=[a_0,a_1,\ldots,a_{n-1}]
$$

Para $n=0$, el resultado es una lista vacía.

Si los primeros $n$ valores son correctos, pedir uno más calcula
$a_n$ mediante la misma regla. Así también son correctos los
primeros $n+1$.

La parte que sigue queda pendiente gracias a `#::`. Por eso pedir
cuatro valores no obliga a calcular una secuencia completa que
no tiene final.

### 4.3. Corrección de `replicasSensibles`

#### Qué debe calcular

Sea $u$ el mínimo y sean $a_0,a_1,\ldots$ los valores de `replicas`.

Se deben conservar los elementos desde el inicio hasta encontrar
el primero que no supere $u$.

Si $k$ es la posición de ese primer valor rechazado:

$$
a_i>u\quad\text{para }0\leq i<k
$$

$$
a_k\leq u
$$

Entonces la respuesta es:

$$
[a_0,a_1,\ldots,a_{k-1}]
$$

Cuando el primer elemento ya incumple la condición, $k=0$ y
la lista queda vacía.

#### Correspondencia con el código

`takeWhile(_ > minimo)` revisa los elementos en orden y conserva
cada uno mientras la comparación sea verdadera.

Al llegar al primero que no cumple, se detiene. Por eso no incluye
valores iguales al mínimo y tampoco continúa buscando después.

`toList` convierte únicamente esa parte seleccionada.

#### Terminación

El código exige un mínimo mayor o igual a cero.

Si la magnitud inicial es positiva, multiplicarla repetidamente
por `0.8` la reduce. El valor entregado termina siendo menor o
igual al mínimo.

Incluso cuando el mínimo es cero, el redondeo acaba produciendo
`0.0`, que ya no cumple la comparación estricta.

Si el primer valor redondeado es menor o igual al mínimo, el
recorrido se detiene desde el inicio.

Por tanto, con las entradas consideradas, se obtiene una lista
finita.

## 5. Corrección de `informe`

#### Qué debe calcular

Para cada región presente, el informe debe reunir:

1. La cantidad de sismos.
2. Su mayor magnitud.
3. El número de réplicas sensibles de esa magnitud máxima.

Llamamos $K(m,u)$ al tamaño de la lista de réplicas sensibles:

$$
K(m,u)=|replicasSensibles(m,u)|
$$

La entrada correspondiente a una región $a$ debe ser:

$$
informe(r,u)(a)=Resumen(C(r,a),M(r,a),K(M(r,a),u))
$$

#### Registro vacío

Si no hay sismos, `conteos` y `maximas` quedan vacíos.
El recorrido no produce entradas y entrega `Map()`.

#### Registro con eventos

Los dos mapas parten del mismo registro, así que contienen los
mismos nombres de región.

Para cada entrada de `maximas`:

- `conteos(region)` obtiene la cantidad correcta.
- `maxima` es la mayor magnitud de ese grupo.
- `replicasSensibles(maxima, minimo)` genera los valores pedidos.
- `length` cuenta cuántos se obtuvieron.

Al construir `Resumen`, los tres datos ocupan el lugar que les
corresponde.

#### Terminación

El registro tiene una cantidad finita de regiones. Para cada una,
el cálculo de réplicas sensibles termina bajo las condiciones
explicadas en el punto 4. Por eso también finaliza el informe.

## 6. Qué ocurre al cambiar el orden del registro

Llamamos $r'$ a una lista con los mismos eventos que $r$, incluyendo
sus repeticiones, pero colocados en otro orden.

### Grupos y conteos

Cada sismo sigue teniendo la misma región, sin importar su posición.
Por eso pertenece al mismo grupo y se conserva la cantidad de
apariciones:

$$
C(r',a)=C(r,a)
$$

El conteo depende de cuántos eventos hay en cada región, no de cuál
se revisó primero.

### Magnitudes máximas

Reordenar los eventos tampoco cambia sus magnitudes. El mayor
valor de cada grupo sigue siendo el mismo:

$$
M(r',a)=M(r,a)
$$

Por ejemplo, las listas `List(4.2, 3.8, 5.3)` y
`List(5.3, 3.8, 4.2)` tienen tres elementos y su máximo es `5.3`.

### Consultas e informe

Como los conteos y máximos se conservan:

- `conteoSeguro` responde las mismas cantidades.
- `maximaDe` encuentra los mismos valores.
- `regionMasActiva` elige el mismo nombre, porque además resuelve
  los empates alfabéticamente.
- `informe` produce los mismos resúmenes para un mínimo fijo.

En particular:

$$
informe(r',u)=informe(r,u)
$$

Las réplicas dependen de la magnitud máxima y del mínimo. Si ambos
datos siguen iguales, también se conserva su cantidad.

### Qué sí puede cambiar

En `porRegion`, cada grupo mantiene los mismos eventos, pero la lista
interna puede quedar en otro orden. Por eso no afirmamos que los dos
mapas de listas sean siempre iguales al compararlos directamente.

`fuertes` y `enRegion` también pueden cambiar el orden de sus respuestas,
porque deben respetar la lista de entrada.

En `paresCercanos` se conservan las mismas parejas de regiones si se
consideran sin orientación. Sin embargo, su presentación puede cambiar,
ya que los nombres se organizan según su primera aparición.

El orden en que se muestran las entradas de un mapa no cambia los
valores asociados a cada región.