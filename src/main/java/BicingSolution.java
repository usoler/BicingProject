import IA.Bicing.Estacion;
import IA.Bicing.Estaciones;

import java.util.Random;

public class BicingSolution {
    private static final int NUM_MAX_BICIS_FURGONETA = 30;

    private static Estaciones estaciones;       // ArrayList de objetos tipo Estacion del problema

    // ------------------------------------------------------------------------
    // Representación de la solución | Coste en memoria: O(4*|F|)
    // ------------------------------------------------------------------------
    private int[] asignaciones;                 // i->id furgoneta, [i]->id estacion [Memoria O(|F|)]
    private int[] realBicisNext;                // i->idEstacion,   [i]->Bicis que habrán en la siguiente hora [Memoria O(|E|)]

    private int[] primerosDestinos;             // i->id furgoneta, [i]->id estacion destino     [Memoria O(|F|)]
    private int[] segundosDestinos;             // i->id furgoneta, [i]->id estacion destino     [Memoria O(|F|)]

    private int[] primerasBicisDejadas;         // i->id furgoneta, [i]->número de bicis dejadas [Memoria O(|F|)]
    private int[] segundasBicisDejadas;         // i->id furgoneta, [i]->número de bicis dejadas [Memoria O(|F|)]

    private double costeTransporte;             // Coste por transporte al final de la hora
    //    private int beneficios;                     // Beneficios en euros obtenidos al final de la hora (se le restan los costes por fallo)
    private int beneficioPorAcierto;
    private int penalizacionPorFallo;

    // ------------------------------------------------------------------------
    // Constructores
    // ------------------------------------------------------------------------

    /**
     * Construye una solución vacía
     *
     * @param numEstaciones número de estaciones del problema
     * @param numBicisTotal número de bicis totales del problema
     * @param numFurgonetas número de furgonetas del problema
     * @param tipoDemanda   tipo de demanda de la hora actual (Equilibrada o Punta)
     * @param semilla       semilla aleatoria
     */
    public BicingSolution(int numEstaciones, int numBicisTotal, int numFurgonetas, TipoDemanda tipoDemanda, int semilla) {
        this.estaciones = new Estaciones(numEstaciones, numBicisTotal, TipoDemanda.getCode(tipoDemanda), semilla);

        initArraysWith(numFurgonetas);

//        this.beneficios = 0;
        this.beneficioPorAcierto = 0;
        this.penalizacionPorFallo = 0;
        this.costeTransporte = 0.0;
    }

    /**
     * Genera una solucion copia de 'solution'
     *
     * @param solution solucion a copiar
     */
    public BicingSolution(BicingSolution solution) {
        this.estaciones = solution.getEstaciones();

        int numFurgonetas = solution.getAsignaciones().length;

        initArraysWith(numFurgonetas);
        copyArraysFrom(solution);

//        this.beneficios = solution.getBeneficios();
        this.beneficioPorAcierto = solution.getBeneficioPorAcierto();
        this.penalizacionPorFallo = solution.getPenalizacionPorFallo();
        this.costeTransporte = solution.getCosteTransporte();
    }

    // ------------------------------------------------------------------------
    // Generadores de solución inicial
    // ------------------------------------------------------------------------

    /**
     * Genera una solucion inicial random
     */
    public void generadorSolucion1() { // Coste total: O(|E|+|F|)
        Random random = new Random();

        boolean[] estacionesAsignadas = new boolean[this.estaciones.size()];
        estacionesAsignadas = inicializarArrayBooleana(estacionesAsignadas);        // O(|E|)

        for (int i = 0; i < this.realBicisNext.length; ++i) {
            realBicisNext[i] = this.estaciones.get(i).getNumBicicletasNext();
        }

        for (int i = 0; i < this.asignaciones.length; ++i) {                        // O(|F|)
            estacionesAsignadas = asignarFurgoneta(i, estacionesAsignadas, random); // O(1)

            if (this.asignaciones[i] == -1) {
                this.primerosDestinos[i] = -1;
                this.segundosDestinos[i] = -1;
                this.primerasBicisDejadas[i] = 0;
                this.segundasBicisDejadas[i] = 0;
            } else {
                asignarDestinos(i, random);         // O(1)
                asignarCargaDestinos(i, random);    // O(1)
                calcularCosteTransporte(i);         // O(1)
            }
        }
    }

    /**
     * Genera una solucion inicial que distribuye las furgonetas disponibles entre las estaciones mas prosperas.
     * Asigna a cada furgoneta entre 0 y 2 estaciones destino con deficit dentro de un radio de distancia de X km.
     */
    /*
    public void generadorSolucion2() {
        int[] estacionesProsperas = initArrayEstacionesProsperas(); // Estaciones ordenadas de mas prospera a menos
        mergeSort(estacionesProsperas, 0, this.estaciones.size() - 1, true); // O(|E|log|E|)

        System.out.println("ESTACIONES PROSPERAS ORDENADAS");
        for (int a = 0; a < estacionesProsperas.length; ++a) {
            System.out.println(String.format("Posicion #%s:  estacion con id '%s'",
                    a, estacionesProsperas[a]));
        }

        int[] estacionesDeficit = obtenerEstacionesConDeficit();
        mergeSort(estacionesDeficit, 0, estacionesDeficit.length - 1, false); // O(|E|log|E|)

        int[] numBicisFaltantes = new int[estacionesDeficit.length];
        numBicisFaltantes = inicializarArrayBicisFaltantes(estacionesDeficit, numBicisFaltantes);

        System.out.println("ESTACIONES CON DEFICIT ORDENADAS");
        for (int b = 0; b < estacionesDeficit.length; ++b) {
            System.out.println(String.format("Posicion #%s:  estacion con id '%s'",
                    b, estacionesDeficit[b]));
        }
        int index = 0;
        for (int i = 0; i < this.asignaciones.length; ++i) {
            asignarFurgonetaEnEstacionProspera(i, estacionesProsperas);

            if (index < numBicisFaltantes.length) {
                asignarDestinosConDeficit(i, estacionesDeficit, index);

                numBicisFaltantes = asignarCargaDestinosConDeficit(i, numBicisFaltantes, index);

                if ((numBicisFaltantes[index] == 0) && (index + 1 < numBicisFaltantes.length) && (numBicisFaltantes[index + 1] == 0)) {
                    System.out.println(String.format("Este indice '%s' y el siguiente '%s' ya cubiertos", index, index + 1));
                    index += 2;
                } else if (numBicisFaltantes[index] == 0) {
                    System.out.println(String.format("Este indice %s ya cubierto", index));
                    ++index;
                }

                calcularCosteTransporte(i);

            } else { // destinos con deficit ya cubiertos
                System.out.println("Todos los destinos con deficit ya cubiertos");
                this.primerosDestinos[i] = -1;
                this.segundosDestinos[i] = -1;
                this.primerasBicisDejadas[i] = 0;
                this.segundasBicisDejadas[i] = 0;
            }

        }

        System.out.println("DISTRIBUCION FURGONETAS");
        for (int j = 0; j < this.asignaciones.length; ++j) {
            System.out.println(String.format("Furgoneta con id '%s' con id estacion origen '%s'",
                    j, this.asignaciones[j]));
        }
    }
*/
    // ------------------------------------------------------------------------
    // OPERADORES:
    // ------------------------------------------------------------------------

    /**
     * Mueve la furgoneta con id 'idFurgoneta' desde su estación de origen actual
     * hasta la estación con id 'idEstacionFinal'
     * <p>
     * Factor ramificación: O(F * E)
     * <p>
     *
     * @param idFurgoneta     id de la furgoneta a mover
     * @param idEstacionFinal id de la estacion a la que mover la furgoneta
     */
    public boolean moverFurgoneta(int idFurgoneta, int idEstacionFinal) {
        if (puedeMoverFurgoneta(idFurgoneta, idEstacionFinal)) {
            if (this.asignaciones[idFurgoneta] == -1) { // Simplemente coloca la furgoneta en la posicion origen final
                this.asignaciones[idFurgoneta] = idEstacionFinal;
                calcularCosteTransporte(idFurgoneta);
                penalizarCostePorFallos(idFurgoneta);
            } else { // en caso de ya tener asignada una posicion de origen
                deshacerCalculoCosteTransporte(idFurgoneta);
                deshacerPenalizarCostePorFallo(idFurgoneta);
                this.asignaciones[idFurgoneta] = idEstacionFinal;
                calcularCosteTransporte(idFurgoneta);
                penalizarCostePorFallos(idFurgoneta);
            }
            return true;
        }
        return false;
    }

    /**
     * Cambia la estación destino actual de la furgoneta con id 'idFurgoneta' por la estación
     * con id 'idEstacionDestinoFinal'
     * <p>
     * Factor ramificación: O(F * E)
     *
     * @param idFurgoneta            id de la furgoneta a cambiar de destino
     * @param destinoUnoODos         destino a cambiar: 1 para el primer destino, 2 para el segundo destino
     * @param idEstacionDestinoFinal id de la estación destino final
     */
    public boolean cambiarEstacionDestino(int idFurgoneta, int destinoUnoODos, int idEstacionDestinoFinal) {
        if (puedeCambiarEstacionDestino(idFurgoneta, destinoUnoODos, idEstacionDestinoFinal)) {
            deshacerBeneficiosPorAciertos(idFurgoneta, destinoUnoODos);
            deshacerCalculoCosteTransporte(idFurgoneta);

            if (destinoUnoODos == 0) this.primerosDestinos[idFurgoneta] = idEstacionDestinoFinal;
            else if (destinoUnoODos == 1) this.segundosDestinos[idFurgoneta] = idEstacionDestinoFinal;
            else System.out.println("Error al pasar el destino Uno o Dos");

            obtenerBeneficiosPorAciertos(idFurgoneta, destinoUnoODos);
            deshacerCalculoCosteTransporte(idFurgoneta);
            return true;
        }
        return false;
    }

    /**
     * Intercambia la furgoneta con id 'idFurgoneta1' por la furgoneta con id 'idFurgoneta2'
     * <p>
     * Factor ramificación O(F * F)
     *
     * @param idFurgoneta1 id de la primera furgoneta a intercambiar
     * @param idFurgoneta2 id de la segunda furgoneta a intercambiar
     */
    public boolean intercambiarFurgonetas(int idFurgoneta1, int idFurgoneta2) { // TODO: Refactor code
        if (puedeIntercambiarFurgonetas(idFurgoneta1, idFurgoneta2)) {
            int idEstacion1 = this.asignaciones[idFurgoneta1];
            int idEstacion2 = this.asignaciones[idFurgoneta2];
            if (idEstacion1 != -1 && idEstacion2 != -1) {
                deshacerCalculoCosteTransporte(idFurgoneta1);
                deshacerPenalizarCostePorFallo(idFurgoneta1);
                this.asignaciones[idFurgoneta1] = -1;
                if (moverFurgoneta(idFurgoneta2, idEstacion1)) {
                    return moverFurgoneta(idFurgoneta1, idEstacion2);
                }
                return false;
            } else if (idEstacion1 == -1 && idEstacion2 != -1) {
                deshacerCalculoCosteTransporte(idFurgoneta2);
                deshacerPenalizarCostePorFallo(idFurgoneta2);
                deshacerBeneficiosPorAciertos(idFurgoneta2, 1);
                deshacerBeneficiosPorAciertos(idFurgoneta2, 2);
                this.asignaciones[idFurgoneta2] = idEstacion1;
                this.primerosDestinos[idFurgoneta2] = -1;
                this.segundosDestinos[idFurgoneta2] = -1;
                this.primerasBicisDejadas[idFurgoneta2] = 0;
                this.segundasBicisDejadas[idFurgoneta2] = 0;

                return (moverFurgoneta(idFurgoneta1, idEstacion2));

            } else if (idEstacion2 == -1 && idEstacion1 != -1) {
                deshacerCalculoCosteTransporte(idFurgoneta1);
                deshacerPenalizarCostePorFallo(idFurgoneta1);
                deshacerBeneficiosPorAciertos(idFurgoneta1, 1);
                deshacerBeneficiosPorAciertos(idFurgoneta1, 2);
                this.asignaciones[idFurgoneta1] = idEstacion1;
                this.primerosDestinos[idFurgoneta1] = -1;
                this.segundosDestinos[idFurgoneta1] = -1;
                this.primerasBicisDejadas[idFurgoneta1] = 0;
                this.segundasBicisDejadas[idFurgoneta1] = 0;

                return (moverFurgoneta(idFurgoneta2, idEstacion1));

            }
        }
        return false;
    }

    /**
     * Cargar furgoneta con id 'idFurgoneta' con 'numBicis1' bicis en su primer destino y 'numBicis2' en su segundo destino
     * <p>
     * Factor ramificación: O(31 * F * F)
     *
     * @param idFurgoneta id de la furgoneta a la que cargar las bicis
     * @param numBicis1   número de bicis que cargar en el destino1
     * @param numBicis2   numero de bicis que cargar en el destino2
     */
    public boolean cargarFurgoneta(int idFurgoneta, int numBicis1, int numBicis2) {
        if (puedeCargarFurgoneta(idFurgoneta, numBicis1, numBicis2)) {

            deshacerPenalizarCostePorFallo(idFurgoneta);
            deshacerBeneficiosPorAciertos(idFurgoneta, 1);
            if (this.segundosDestinos[idFurgoneta] != -1) {
                deshacerBeneficiosPorAciertos(idFurgoneta, 2);
            }
            deshacerCalculoCosteTransporte(idFurgoneta);

            this.primerasBicisDejadas[idFurgoneta] = numBicis1;
            if (this.segundosDestinos[idFurgoneta] != -1) {
                this.segundasBicisDejadas[idFurgoneta] = numBicis2;
            }

            penalizarCostePorFallos(idFurgoneta);
            obtenerBeneficiosPorAciertos(idFurgoneta, 0);
            if (this.segundosDestinos[idFurgoneta] != -1) {
                obtenerBeneficiosPorAciertos(idFurgoneta, 1);
            }
            calcularCosteTransporte(idFurgoneta);

            return true;
        }

        return false;
    }

    /**
     * Devuelve true si se puede mover la furgoneta idFurgoneta a la estación idEstacionFinal
     * <p>
     *
     * @param idFurgoneta     id de la furgoneta
     * @param idEstacionFinal id de la estación a la que queremos mover la furgoneta
     */
    private boolean puedeMoverFurgoneta(int idFurgoneta, int idEstacionFinal) {
        int idEstacionOrigenActual = this.asignaciones[idFurgoneta];
        int cargaFurgoneta = this.primerasBicisDejadas[idFurgoneta] + this.segundasBicisDejadas[idFurgoneta];
        int bicisDisponiblesEstacionFinal = this.estaciones.get(idEstacionFinal).getNumBicicletasNoUsadas();
        //Comprobamos que la estación destino no esté ocupada
        boolean ocupada = false;
        for (int i = 0; i < this.asignaciones.length; ++i) {
            if (this.asignaciones[i] == idEstacionFinal) ocupada = true;
        }

        return ((idEstacionFinal != idEstacionOrigenActual) && (cargaFurgoneta <= bicisDisponiblesEstacionFinal) && !ocupada);
    }

    /**
     * Devuelve true si se puede cambiar el destino (1 o 2) de la furgoneta idFurgoneta a la estación idEstacionDestinoFinal
     * <p>
     *
     * @param idFurgoneta            id de la furgoneta
     * @param destinoUnoODos         1=primer destino 2= segundo destino
     * @param idEstacionDestinoFinal id de la estación a la que queremos mover la furgoneta
     */
    private boolean puedeCambiarEstacionDestino(int idFurgoneta, int destinoUnoODos, int idEstacionDestinoFinal) {
        int idEstacionOrigen = this.asignaciones[idFurgoneta];

        if (idEstacionOrigen == -1) {
            return false;
        }

        if (idEstacionOrigen == idEstacionDestinoFinal) {
            return false;
        }

        if ((this.primerosDestinos[idFurgoneta] != -1) && (this.segundosDestinos[idFurgoneta] != -1)) {
            if ((destinoUnoODos == 1) && (idEstacionDestinoFinal == this.segundosDestinos[idFurgoneta])) {
                return false;
            }

            if ((destinoUnoODos == 2) && (idEstacionDestinoFinal == this.primerosDestinos[idFurgoneta])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Devuelve true si se puede intercambiar el origen de las furgonetas idFurgoneta1 y idFurgoneta2
     * <p>
     *
     * @param idFurgoneta1 id de la furgoneta1
     * @param idFurgoneta2 id de la furgoneta2
     */
    private boolean puedeIntercambiarFurgonetas(int idFurgoneta1, int idFurgoneta2) {
        int idEstacionFurgoneta1 = this.asignaciones[idFurgoneta1];
        int idEstacionFurgoneta2 = this.asignaciones[idFurgoneta2];

        if (idEstacionFurgoneta1 == -1 && idEstacionFurgoneta2 == -1) {
            return false;
        } else if (idEstacionFurgoneta1 == -1) {
            int cargaFurgoneta1 = this.primerasBicisDejadas[idFurgoneta1] + this.segundasBicisDejadas[idFurgoneta1];
            int bicisDisponiblesEstacionFurgoneta2 = this.estaciones.get(idEstacionFurgoneta2).getNumBicicletasNoUsadas();
            return (cargaFurgoneta1 <= bicisDisponiblesEstacionFurgoneta2);
        } else if (idEstacionFurgoneta2 == -1) {
            int cargaFurgoneta2 = this.primerasBicisDejadas[idFurgoneta2] + this.segundasBicisDejadas[idFurgoneta2];
            int bicisDisponiblesEstacionFurgoneta1 = this.estaciones.get(idEstacionFurgoneta1).getNumBicicletasNoUsadas();
            return (cargaFurgoneta2 <= bicisDisponiblesEstacionFurgoneta1);
        } else {
            int cargaFurgoneta1 = this.primerasBicisDejadas[idFurgoneta1] + this.segundasBicisDejadas[idFurgoneta1];
            int cargaFurgoneta2 = this.primerasBicisDejadas[idFurgoneta2] + this.segundasBicisDejadas[idFurgoneta2];
            int bicisDisponiblesEstacionFurgoneta1 = this.estaciones.get(idEstacionFurgoneta2).getNumBicicletasNoUsadas();
            int bicisDisponiblesEstacionFurgoneta2 = this.estaciones.get(idEstacionFurgoneta1).getNumBicicletasNoUsadas();

            return ((cargaFurgoneta1 <= bicisDisponiblesEstacionFurgoneta2) && (cargaFurgoneta2 <= bicisDisponiblesEstacionFurgoneta1));
        }
    }

    /**
     * Devuelve true si se puede cargar la furgoneta idFurgoneta con numBicis1 en el destino 1 y numBicis2 en el destino 2
     * <p>
     *
     * @param idFurgoneta id de la furgoneta
     * @param numBicis1   # bicis que dejar en el destino 1
     * @param numBicis2   # bicis que dejar en el destino 2
     */
    private boolean puedeCargarFurgoneta(int idFurgoneta, int numBicis1, int numBicis2) {
        if (this.primerosDestinos[idFurgoneta] == -1) {
            return false;
        } else if (this.segundosDestinos[idFurgoneta] == -1 && numBicis2 > 0) {
            return false;
        }
        int cargaTotalBicis = numBicis1 + numBicis2;

        if ((cargaTotalBicis > 30) || (this.estaciones.get(this.asignaciones[idFurgoneta]).getNumBicicletasNoUsadas() < cargaTotalBicis)) {
            return false;
        }

        return true;
    }


    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------
    public Estaciones getEstaciones() {
        return this.estaciones;
    }

    public int[] getAsignaciones() {
        return this.asignaciones;
    }

    public int[] getRealBicisNext() {
        return this.realBicisNext;
    }

    public int[] getPrimerosDestinos() {
        return this.primerosDestinos;
    }

    public int[] getSegundosDestinos() {
        return this.segundosDestinos;
    }

    public int[] getPrimerasBicisDejadas() {
        return this.primerasBicisDejadas;
    }

    public int[] getSegundasBicisDejadas() {
        return this.segundasBicisDejadas;
    }

    //    public int getBeneficios() {
//        return this.beneficios;
//    }
    public int getBeneficioPorAcierto() {
        return this.beneficioPorAcierto;
    }

    public int getPenalizacionPorFallo() {
        return this.penalizacionPorFallo;
    }

    public double getCosteTransporte() {
        return this.costeTransporte;
    }

    // ------------------------------------------------------------------------
    // Métodos auxiliares
    // ------------------------------------------------------------------------
    public boolean isGoalState() {
        return false;
    }

    private boolean[] inicializarArrayBooleana(boolean[] array) { //O(|E|)
        for (int i = 0; i < array.length; ++i) {
            array[i] = false;
        }

        return array;
    }

    private boolean[] asignarFurgoneta(int idFurgoneta, boolean[] estacionesAsignadas, Random random) { // O(1)
        int idEstacionRandom = random.nextInt(estacionesAsignadas.length);
        System.out.println(String.format("Asignando a la furgoneta con id '%s' la estacion origen random '%s'",
                idFurgoneta, idEstacionRandom));

        if (idEstacionRandom != (estacionesAsignadas.length + 1) && !estacionesAsignadas[idEstacionRandom]) {
            estacionesAsignadas[idEstacionRandom] = true;
        } else {
            idEstacionRandom = -1;
        }

        this.asignaciones[idFurgoneta] = idEstacionRandom;
        System.out.println(String.format("Asignada la estacion origen random '%s'", this.asignaciones[idFurgoneta]));

        return estacionesAsignadas;
    }

   /* private void asignarFurgonetaEnEstacionProspera(int idFurgoneta, int[] estacionesProsperasAsignadas) {
        System.out.println(String.format("Asignando a furgoneta con id '%s' la estacion con id '%s'",
                idFurgoneta, estacionesProsperasAsignadas[idFurgoneta]));

        this.asignaciones[idFurgoneta] = estacionesProsperasAsignadas[idFurgoneta];

        System.out.println(String.format("Asignada a furgoneta con id '%s' la estacion con id '%s'",
                idFurgoneta, this.asignaciones[idFurgoneta]));
    }*/

    private void asignarDestinos(int idFurgoneta, Random random) { // O(1)
        int numTotalEstaciones = this.estaciones.size(); // 0 <= id estacion < numTotalEstaciones
        int idEstacionOrigen = this.asignaciones[idFurgoneta];
        int idDestino1Random = random.nextInt(numTotalEstaciones);
        System.out.println(String.format("Asignando a la furgoneta con id '%s' la estacion destino random '%s'",
                idFurgoneta, idDestino1Random));

        if ((idDestino1Random < numTotalEstaciones) && (idDestino1Random != idEstacionOrigen)) {
            this.primerosDestinos[idFurgoneta] = idDestino1Random;
            System.out.println(String.format("Asignada la estacion destino1 random '%s'",
                    this.primerosDestinos[idFurgoneta]));
            int idDestino2Random = random.nextInt(numTotalEstaciones);
            System.out.println(String.format("Asignando la estacion destino2 random '%s'", idDestino2Random));
            if ((idDestino2Random < numTotalEstaciones) &&
                    (this.primerosDestinos[idFurgoneta] != idDestino2Random) &&
                    (idDestino2Random != this.asignaciones[idFurgoneta])) {
                this.segundosDestinos[idFurgoneta] = idDestino2Random;
                System.out.println(String.format("Asignada la estacion destino2 random '%s'",
                        this.segundosDestinos[idFurgoneta]));
            } else {
                this.segundosDestinos[idFurgoneta] = -1;
                System.out.println(String.format("Asignada la estacion destino2 random '%s'", -1));
            }
        } else {
            this.segundosDestinos[idFurgoneta] = -1;
            System.out.println(String.format("Asignada la estacion destino2 random '%s'",
                    this.segundosDestinos[idFurgoneta]));
            idDestino1Random = random.nextInt(numTotalEstaciones);
            System.out.println(String.format("Asignando la estacion destino1 random '%s'", idDestino1Random));
            if ((idDestino1Random < numTotalEstaciones) && (idDestino1Random != idEstacionOrigen)) {
                this.primerosDestinos[idFurgoneta] = idDestino1Random;
            } else {
                this.primerosDestinos[idFurgoneta] = -1;
            }
            System.out.println(String.format("Asignada la estacion destino1 random '%s'",
                    this.primerosDestinos[idFurgoneta]));
        }
    }

    private void asignarDestinosConDeficit(int idFurgoneta, int[] estacionesDeficit, int index) {
        System.out.println(String.format("Asignando al destino1 de la furgoneta con id '%s' la estacion " +
                "con deficit '%s' (index = %s)", idFurgoneta, estacionesDeficit[index], index));
        this.primerosDestinos[idFurgoneta] = estacionesDeficit[index];

        if (index < estacionesDeficit.length - 1) {
            System.out.println(String.format("Asignando al destino2 la estacion con deficit '%s' (index = %s)",
                    estacionesDeficit[index + 1], index));
            this.segundosDestinos[idFurgoneta] = estacionesDeficit[index + 1];
        } else {
            this.segundosDestinos[idFurgoneta] = -1;
        }
    }

    /**
     * Devuelve true si se puede cargar la furgoneta idFurgoneta con numBicis1 en el destino 1 y numBicis2 en el destino 2
     * <p>
     *
     * @param idFurgoneta id de la furgoneta
     * @param random      # bicis que dejar en el destino 1
     */
    private void asignarCargaDestinos(int idFurgoneta, Random random) { // O(1)
        int idEstacionOrigen = this.asignaciones[idFurgoneta];
        if (idEstacionOrigen != -1) {
            if (primerosDestinos[idFurgoneta] != -1) {
                int cargaMax1 = NUM_MAX_BICIS_FURGONETA;
                int noUsadas = this.estaciones.get(idEstacionOrigen).getNumBicicletasNoUsadas();
                System.out.println(String.format("Bicis no usadas: '%s'", noUsadas));
                if (noUsadas < cargaMax1) cargaMax1 = noUsadas;

                int carga1 = 0;
                int carga2 = 0;
                if (cargaMax1 > 0) {
                    carga1 = random.nextInt(cargaMax1); // Random.nextInt(value) necesita que 'value' > 0, si no lanza excepcion
                    this.primerasBicisDejadas[idFurgoneta] = carga1;
                    System.out.println(String.format("Asignado al destino1 '%s' bicis", carga1));
                    obtenerBeneficiosPorAciertos(idFurgoneta, 0);

                    if ((segundosDestinos[idFurgoneta] != -1) && ((cargaMax1 - carga1) > 0)) {
                        carga2 = random.nextInt(cargaMax1 - carga1);
                        this.segundasBicisDejadas[idFurgoneta] = carga2;
                        System.out.println(String.format("Asignado al destino2 '%s' bicis", carga2));
                        obtenerBeneficiosPorAciertos(idFurgoneta, 1);
                    }

                    penalizarCostePorFallos(idFurgoneta);
                }

                System.out.println(String.format("Asignada a la furgoneta con id '%s' en la estacion de origen con id " +
                                "'%s', una carga: '%s' para el primer destino y una carga: '%s' para el segundo",
                        idFurgoneta, idEstacionOrigen, carga1, carga2));
            }
        }
    }
/*
    private int[] asignarCargaDestinosConDeficit(int idFurgoneta, int[] numBicisFaltantes, int index) {
        System.out.println(String.format("Asignando carga destino1 a furgoenta con id '%s', a estacion " +
                "con num bicis faltantes '%s' (index = %s)", idFurgoneta, numBicisFaltantes[index], index));
        int demandaOrigen = this.estaciones.get(this.asignaciones[idFurgoneta]).getDemanda();
        int bicisDisponiblesOrigen = this.estaciones.get(this.asignaciones[idFurgoneta]).getNumBicicletasNext();
        int bicisParaCargar = bicisDisponiblesOrigen - demandaOrigen;
        System.out.println(String.format("Demanda estacion origen = %s", demandaOrigen));
        System.out.println(String.format("Bicis disponibles origen = %s", bicisDisponiblesOrigen));
        System.out.println(String.format("Bicis para cargar = %s", bicisParaCargar));

        if (bicisParaCargar > 30) {
            bicisParaCargar = 30;
            System.out.println("Bicis reducidas a 30");
        }

        if (numBicisFaltantes[index] <= 30) {
            System.out.println(String.format("Faltan menos de 30 bicis (%s faltantes)", numBicisFaltantes[index]));
            if (bicisParaCargar >= numBicisFaltantes[index]) { // dejo en el destino las que hacen falta (puede que tenga para un segundo destino)
                System.out.println(String.format("Tengo '%s' bicis para cargar >= '%s' bicis faltantes",
                        bicisParaCargar, numBicisFaltantes[index]));
                this.primerasBicisDejadas[idFurgoneta] = numBicisFaltantes[index];
                bicisParaCargar -= numBicisFaltantes[index];
                numBicisFaltantes[index] = 0;
                System.out.println(String.format("Dejamos en el primer destino '%s' bicis",
                        this.primerasBicisDejadas[idFurgoneta]));
                System.out.println(String.format("Bicis para cargar en un posible segundo destino = %s",
                        bicisParaCargar));
                System.out.println(String.format("Bicis faltantes = %s", numBicisFaltantes[index]));

                if ((bicisParaCargar > 0) && (this.segundosDestinos[idFurgoneta] != -1)) { // si tiene un segundo destino asignado => index+1 es posible
                    System.out.println("Puede asignar bicis a un segundo destino");
                    if (bicisParaCargar >= numBicisFaltantes[index + 1]) {
                        System.out.println(String.format("Bicis para cargar '%s' >= bicis faltantes '%s'",
                                bicisParaCargar, numBicisFaltantes[index + 1]));
                        this.segundasBicisDejadas[idFurgoneta] = numBicisFaltantes[index + 1];
                        bicisParaCargar -= numBicisFaltantes[index + 1];
                        numBicisFaltantes[index + 1] = 0;
                        System.out.println(String.format("Dejamos en el segundo destino '%s' bicis",
                                this.segundasBicisDejadas[idFurgoneta]));
                        System.out.println(String.format("Bicis restantes no usadas = %s", bicisParaCargar));
                        System.out.println(String.format("Bicis faltantes = %s", numBicisFaltantes[index + 1]));
                    } else {
                        System.out.println(String.format("Bicis para cargar '%s' < bicis faltantes '%s'",
                                bicisParaCargar, numBicisFaltantes[index + 1]));
                        this.segundasBicisDejadas[idFurgoneta] = bicisParaCargar;
                        bicisParaCargar = 0;
                        numBicisFaltantes[index + 1] -= this.segundasBicisDejadas[idFurgoneta];
                        System.out.println(String.format("Dejamos en el segundo destino '%s'",
                                this.segundasBicisDejadas[idFurgoneta]));
                        System.out.println(String.format("Bicis restantes no usadas = %s", bicisParaCargar));
                        System.out.println(String.format("Bicis faltantes = %s", numBicisFaltantes[index + 1]));
                    }
                } else { // no quedan mas bicis => no tiene segundo destino
                    System.out.println("No quedan mas bicis o no tiene un segundo destino asignado");
                    this.segundosDestinos[idFurgoneta] = -1;
                }
            } else { // dejo en el destino todas las que puedo cargar (no tendra segundo destino)
                System.out.println(String.format("Bicis para cargar '%s' < bicis faltantes '%s'",
                        bicisParaCargar, numBicisFaltantes[index]));
                this.primerasBicisDejadas[idFurgoneta] = bicisParaCargar;
                bicisParaCargar = 0;
                numBicisFaltantes[index] -= this.primerasBicisDejadas[idFurgoneta];
                this.segundosDestinos[idFurgoneta] = -1;

                System.out.println(String.format("Dejamos en el primer destino '%s' bicis",
                        this.primerasBicisDejadas[idFurgoneta]));
                System.out.println(String.format("Bicis restantes para cargar = %s", bicisParaCargar));
                System.out.println(String.format("Bicis faltantes = %s", numBicisFaltantes[index]));
            }
        } else { // dejo en el destino todas las que puedo cargar (no tendra segundo destino)
            System.out.println(String.format("Tengo mas de 30 bicis faltantes ('%s' faltantes)",
                    numBicisFaltantes[index]));
            this.primerasBicisDejadas[idFurgoneta] = bicisParaCargar;
            bicisParaCargar = 0;
            numBicisFaltantes[index] -= this.primerasBicisDejadas[idFurgoneta];
            this.segundosDestinos[idFurgoneta] = -1;

            System.out.println(String.format("Dejamos en el primer destino '%s' bicis",
                    this.primerasBicisDejadas[idFurgoneta]));
            System.out.println(String.format("Bicis para cargar restantes = %s", bicisParaCargar));
            System.out.println(String.format("Bicis faltantes = %s", numBicisFaltantes[index]));
        }

        if ((this.primerosDestinos[idFurgoneta] != 1) && (this.primerasBicisDejadas[idFurgoneta] > 0)) {
            int demandaDestino1 = this.estaciones.get(this.primerosDestinos[idFurgoneta]).getDemanda();
            int bicisDisponiblesDestino1 = this.estaciones.get(this.primerosDestinos[idFurgoneta]).getNumBicicletasNext();

            obtenerBeneficiosPorAciertos(demandaDestino1, bicisDisponiblesDestino1, this.primerasBicisDejadas[idFurgoneta]);

            if ((this.segundosDestinos[idFurgoneta] != -1) && (this.segundasBicisDejadas[idFurgoneta] > 0)) {
                int demandaDestino2 = this.estaciones.get(this.segundosDestinos[idFurgoneta]).getDemanda();
                int bicisDisponiblesDestino2 = this.estaciones.get(this.segundosDestinos[idFurgoneta]).getNumBicicletasNext();

                obtenerBeneficiosPorAciertos(demandaDestino2, bicisDisponiblesDestino2, this.segundasBicisDejadas[idFurgoneta]);
            }
        }

        penalizarCostePorFallos(demandaOrigen, bicisDisponiblesOrigen,
                this.primerasBicisDejadas[idFurgoneta] + this.segundasBicisDejadas[idFurgoneta]);

        return numBicisFaltantes;
    }
*/

    //CÁLCULO DE BENEFICIOS Y COSTES:

    /**
     * Penaliza el coste por los fallos de la furgoneta con idFurgoneta y con carga x en la estación de origen de la furgoneta
     * <p>
     *
     * @param idFurgoneta id de la furgoneta
     */

    private void penalizarCostePorFallos(int idFurgoneta) {      // O(1)
        System.out.println("Penalizamos fallos");
        int cargaFurgoneta = this.primerasBicisDejadas[idFurgoneta] + this.segundasBicisDejadas[idFurgoneta];
        Estacion estacionOrigen = this.estaciones.get(this.asignaciones[idFurgoneta]);
        int maxBicisSinPen = this.realBicisNext[this.asignaciones[idFurgoneta]] - estacionOrigen.getDemanda();

        if (maxBicisSinPen <= 0) {
            System.out.println(String.format("Penalizacion por fallos (cargaFurgoneta): '%s'", cargaFurgoneta));
//            this.beneficios -= cargaFurgoneta;
            this.penalizacionPorFallo += cargaFurgoneta;
        } else if (cargaFurgoneta > maxBicisSinPen) {
            //Resta solamente las bicis que nos alejamos de más de la demanda
            System.out.println("Fallos: '%s'" + (cargaFurgoneta - maxBicisSinPen));
            System.out.println(String.format("Penalizacion por fallos (cargaFurgoneta - maxBicisSinPen): '%s'", (cargaFurgoneta - maxBicisSinPen)));
//            this.beneficios -= (cargaFurgoneta - maxBicisSinPen);
            this.penalizacionPorFallo += (cargaFurgoneta - maxBicisSinPen);
        } else {
            System.out.println("No hay ninguna penalizacion por fallos");
        }

        this.realBicisNext[this.asignaciones[idFurgoneta]] -= cargaFurgoneta;

    }

    /**
     * Suma los beneficios que obtenemos de enviar la furgoneta al destino seleccionado
     * <p>
     *
     * @param idFurgoneta    id de la furgoneta
     * @param destinoUnoODos 1 si queremos mirar el primer destino, 2 para el segundo
     */
    private void obtenerBeneficiosPorAciertos(int idFurgoneta, int destinoUnoODos) {        // O(1)
        System.out.println("Obtenemos beneficios por aciertos");
        Estacion estacionDestino;
        int estacionID;
        int cargaADejar;
        if (destinoUnoODos == 0) {
            estacionDestino = this.estaciones.get(this.primerosDestinos[idFurgoneta]);
            cargaADejar = primerasBicisDejadas[idFurgoneta];
            estacionID = this.primerosDestinos[idFurgoneta];
        } else if (destinoUnoODos == 1) {
            estacionDestino = this.estaciones.get(this.segundosDestinos[idFurgoneta]);
            cargaADejar = segundasBicisDejadas[idFurgoneta];
            estacionID = this.segundosDestinos[idFurgoneta];
        } else {
            System.out.println("Error al seleccionar el destino de la función obterBeneficiosPorAciertos");
            return;
        }

        //Si puede haber beneficios (Demanda > next)
        int maxBeneficios = estacionDestino.getDemanda() - this.realBicisNext[estacionID];
        System.out.println(String.format("Demanda: '%s'", estacionDestino.getDemanda()));
        System.out.println(String.format("RealBicisNext: '%s'", this.realBicisNext[estacionID]));
        if (maxBeneficios > 0) {
            //Si tenemos mas carga que beneficios posibles, los beneficios es el max beneficios, sino los beneficios son la carga
            if (cargaADejar > maxBeneficios) {
//                this.beneficios += maxBeneficios;
                this.beneficioPorAcierto += maxBeneficios;
                System.out.println(String.format("Beneficio a acumular (maxBeneficios): '%s'", maxBeneficios));
            } else {
//                this.beneficios += cargaADejar;
                this.beneficioPorAcierto += cargaADejar;
                System.out.println(String.format("Beneficio a acumular (cargaADejar): '%s'", cargaADejar));
            }
        } else {
            System.out.println("No hay beneficios posibles");
        }

        this.realBicisNext[estacionID] += cargaADejar;
    }

    /**
     * Resta los beneficios que obtenemos de enviar la furgoneta al destino seleccionado. Esta función se llama
     * antes de cambiar un destino.
     * <p>
     *
     * @param idFurgoneta    id de la furgoneta
     * @param destinoUnoODos 1 si queremos mirar el primer destino, 2 para el segundo
     */
    private void deshacerBeneficiosPorAciertos(int idFurgoneta, int destinoUnoODos) {        // O(1)
        System.out.println("Restamos beneficios por aciertos");
        Estacion estacionDestino;
        int estacionID;
        int cargaADejar;
        if (destinoUnoODos == 1) {
            estacionDestino = this.estaciones.get(this.primerosDestinos[idFurgoneta]);
            cargaADejar = primerasBicisDejadas[idFurgoneta];
            estacionID = this.primerosDestinos[idFurgoneta];
        } else if (destinoUnoODos == 2) {
            estacionDestino = this.estaciones.get(this.segundosDestinos[idFurgoneta]);
            cargaADejar = segundasBicisDejadas[idFurgoneta];
            estacionID = this.segundosDestinos[idFurgoneta];
        } else {
            System.out.println("Error al seleccionar el destino de la función obterBeneficiosPorAciertos");
            return;
        }

        this.realBicisNext[estacionID] -= cargaADejar;
        int maxBeneficios = estacionDestino.getDemanda() - this.realBicisNext[estacionID];
        if (maxBeneficios > 0) {
            //Si tenemos mas carga que beneficios posibles, los beneficios es el max beneficios, sino los beneficios son la carga
            if (cargaADejar > maxBeneficios) {
//                this.beneficios -= maxBeneficios;
                this.beneficioPorAcierto -= maxBeneficios;
                System.out.println(String.format("Deshacemos beneficio a acumular (maxBeneficios): '%s'", maxBeneficios));
            } else {
//                this.beneficios -= cargaADejar;
                this.beneficioPorAcierto -= cargaADejar;
                System.out.println(String.format("Deshacemos beneficio a acumular (cargaADejar): '%s'", maxBeneficios));
            }
        }

    }


    /**
     * Calcula el coste de transporte que tendrá una furgoneta con idFurgoneta. (nb + 9)/10
     * <p>
     *
     * @param idFurgoneta id de la furgoneta
     */
    private void calcularCosteTransporte(int idFurgoneta) {         // O(1)
        int idOrigen = this.asignaciones[idFurgoneta];
        int idDestino1 = this.primerosDestinos[idFurgoneta];
        int idDestino2 = this.segundosDestinos[idFurgoneta];
        System.out.println(String.format("Estacion origen = '%s'", idOrigen));
        System.out.println(String.format("Destino1 = '%s'", idDestino1));
        System.out.println(String.format("Destino2 = '%s'", idDestino2));
        if ((idOrigen != -1) && (idDestino1 != -1)) {
            System.out.println(String.format("Calculo del coste de transporte del primer destino:"));
            Estacion estacionOrigen = this.estaciones.get(idOrigen);
            int ix = estacionOrigen.getCoordX();
            int iy = estacionOrigen.getCoordY();
            System.out.println(String.format("ix = '%s'", ix));
            System.out.println(String.format("iy = '%s'", iy));
            Estacion estacionDestino1 = this.estaciones.get(idDestino1);
            int jx = estacionDestino1.getCoordX();
            int jy = estacionDestino1.getCoordY();
            System.out.println(String.format("jx = '%s'", jx));
            System.out.println(String.format("jy = '%s'", jy));

            double distanciaEnMetros = Math.abs(ix - jx) + Math.abs(iy - jy);
            System.out.println(String.format("Distancia(metros) = '%s'", distanciaEnMetros));

            int cargaTotalBicis = this.primerasBicisDejadas[idFurgoneta];

            if (idDestino2 != -1) {
                cargaTotalBicis += this.segundasBicisDejadas[idFurgoneta];
            }
            System.out.println(String.format("Carga total bicis = '%s'", cargaTotalBicis));

            double coste = ((double) (cargaTotalBicis + 9) / 10); // euros/km
            System.out.println(String.format("Coste primer destino = '%s'", coste));

            this.costeTransporte += (coste * (distanciaEnMetros / 1000));
            System.out.println(String.format("Coste de transporte total anadido = '%s'", (coste * (distanciaEnMetros / 1000))));

            if (idDestino2 != -1) {
                System.out.println(String.format("Calculo del coste de transporte del segundo destino:"));
                Estacion estacionDestino2 = this.estaciones.get(idDestino2);
                int kx = estacionDestino2.getCoordX();
                int ky = estacionDestino2.getCoordY();
                System.out.println(String.format("kx = '%s'", kx));
                System.out.println(String.format("ky = '%s'", ky));

                distanciaEnMetros = Math.abs(jx - kx) + Math.abs(jy - ky);
                System.out.println(String.format("Distancia(metros) = '%s'", distanciaEnMetros));

                cargaTotalBicis -= this.primerasBicisDejadas[idFurgoneta];
                System.out.println(String.format("Carga total bicis = '%s'", cargaTotalBicis));


                coste = ((double) (cargaTotalBicis + 9) / 10); // euros/km
                System.out.println(String.format("Coste primer destino = '%s'", coste));

                this.costeTransporte += (coste * (distanciaEnMetros / 1000));
                System.out.println(String.format("Coste de transporte total anadido = '%s'", (coste * (distanciaEnMetros / 1000))));
            }
        }
    }


    /**
     * Deshace el coste por los fallos de la furgoneta con idFurgoneta
     * <p>
     *
     * @param idFurgoneta id de la furgoneta a la que cargar las bicis
     */

    private void deshacerPenalizarCostePorFallo(int idFurgoneta) { // TODO: Refactor code
        System.out.println("Deshacemos la penalización por fallos");
        int cargaFurgoneta = this.primerasBicisDejadas[idFurgoneta] + this.segundasBicisDejadas[idFurgoneta];
        this.realBicisNext[this.asignaciones[idFurgoneta]] += cargaFurgoneta;

        Estacion estacionOrigen = this.estaciones.get(this.asignaciones[idFurgoneta]);
        int hueco = this.realBicisNext[this.asignaciones[idFurgoneta]] - estacionOrigen.getDemanda();

        if (hueco < 0) {
            System.out.println(String.format("Deshacemos penalizacion (cargaFurgoneta): '%s'", cargaFurgoneta));
//            this.beneficios += cargaFurgoneta;
            this.penalizacionPorFallo -= cargaFurgoneta;
        } else if (cargaFurgoneta > hueco) {
            //Resta solamente las bicis que nos alejamos de más de la demanda
            System.out.println(String.format("Deshacemos penalizacion (cargaFurgoneta-hueco): '%s'", (cargaFurgoneta - hueco)));
            System.out.println("Deshacemos '%s' fallos" + (cargaFurgoneta - hueco));
//            this.beneficios += (cargaFurgoneta - hueco);
            this.penalizacionPorFallo -= (cargaFurgoneta - hueco);
        } else {
            System.out.println("Fallos: 0");
        }
    }

    /**
     * Deshace el calculo de transporte de la furgoneta con idFurgoneta
     * <p>
     *
     * @param idFurgoneta id de la furgoneta a la que cargar las bicis
     */
    private void deshacerCalculoCosteTransporte(int idFurgoneta) {         // O(1)
        int idOrigen = this.asignaciones[idFurgoneta];
        int idDestino1 = this.primerosDestinos[idFurgoneta];
        int idDestino2 = this.segundosDestinos[idFurgoneta];
        System.out.println(String.format("Estacion origen = '%s'", idOrigen));
        System.out.println(String.format("Destino1 = '%s'", idDestino1));
        System.out.println(String.format("Destino2 = '%s'", idDestino2));
        if ((idOrigen != -1) && (idDestino1 != -1)) {
            System.out.println(String.format("Calculo del coste de transporte del primer destino:"));
            Estacion estacionOrigen = this.estaciones.get(idOrigen);
            int ix = estacionOrigen.getCoordX();
            int iy = estacionOrigen.getCoordY();
            System.out.println(String.format("ix = '%s'", ix));
            System.out.println(String.format("iy = '%s'", iy));
            Estacion estacionDestino1 = this.estaciones.get(idDestino1);
            int jx = estacionDestino1.getCoordX();
            int jy = estacionDestino1.getCoordY();
            System.out.println(String.format("jx = '%s'", jx));
            System.out.println(String.format("jy = '%s'", jy));

            double distanciaEnMetros = Math.abs(ix - jx) + Math.abs(iy - jy);
            System.out.println(String.format("Distancia(metros) = '%s'", distanciaEnMetros));

            int cargaTotalBicis = this.primerasBicisDejadas[idFurgoneta];

            if (idDestino2 != -1) {
                cargaTotalBicis += this.segundasBicisDejadas[idFurgoneta];
            }
            System.out.println(String.format("Carga total bicis = '%s'", cargaTotalBicis));

            double coste = ((double) (cargaTotalBicis + 9) / 10); // euros/km
            System.out.println(String.format("Coste primer destino = '%s'", coste));

            this.costeTransporte -= (coste * (distanciaEnMetros / 1000));
            System.out.println(String.format("Coste de transporte total anadido = '%s'", (coste * (distanciaEnMetros / 1000))));

            if (idDestino2 != -1) {
                System.out.println(String.format("Calculo del coste de transporte del segundo destino:"));
                Estacion estacionDestino2 = this.estaciones.get(idDestino2);
                int kx = estacionDestino2.getCoordX();
                int ky = estacionDestino2.getCoordY();
                System.out.println(String.format("kx = '%s'", kx));
                System.out.println(String.format("ky = '%s'", ky));

                distanciaEnMetros = Math.abs(jx - kx) + Math.abs(jy - ky);
                System.out.println(String.format("Distancia(metros) = '%s'", distanciaEnMetros));

                cargaTotalBicis -= this.primerasBicisDejadas[idFurgoneta];
                System.out.println(String.format("Carga total bicis = '%s'", cargaTotalBicis));


                coste = ((double) (cargaTotalBicis + 9) / 10); // euros/km
                System.out.println(String.format("Coste primer destino = '%s'", coste));

                this.costeTransporte -= (coste * (distanciaEnMetros / 1000));
                System.out.println(String.format("Coste de transporte total anadido = '%s'", (coste * (distanciaEnMetros / 1000))));
            }
        }
    }

    private void initArraysWith(int numFurgonetas) { // O(1)
        this.asignaciones = new int[numFurgonetas];

        this.primerosDestinos = new int[numFurgonetas];
        this.segundosDestinos = new int[numFurgonetas];

        this.primerasBicisDejadas = new int[numFurgonetas];
        this.segundasBicisDejadas = new int[numFurgonetas];

        this.realBicisNext = new int[getEstaciones().size()];
    }

    private int[] initArrayEstacionesProsperas() {
        int[] array = new int[this.estaciones.size()];

        for (int i = 0; i < array.length; ++i) {
            array[i] = i;
        }

        return array;
    }

    private void copyArraysFrom(BicingSolution solution) { // O(1)
        System.arraycopy(solution.getAsignaciones(), 0, this.asignaciones, 0, solution.getAsignaciones().length);
        System.arraycopy(solution.getRealBicisNext(), 0, this.realBicisNext, 0, solution.getRealBicisNext().length);
        System.arraycopy(solution.getPrimerosDestinos(), 0, this.primerosDestinos, 0, solution.getPrimerosDestinos().length);
        System.arraycopy(solution.getSegundosDestinos(), 0, this.segundosDestinos, 0, solution.getSegundosDestinos().length);
        System.arraycopy(solution.getPrimerasBicisDejadas(), 0, this.primerasBicisDejadas, 0, solution.getPrimerasBicisDejadas().length);
        System.arraycopy(solution.getSegundasBicisDejadas(), 0, this.segundasBicisDejadas, 0, solution.getSegundasBicisDejadas().length);
    }

    /*private void mergeSort(int[] array, int start, int end, boolean ordenProspero) { // O(nlogn)
        if (start < end) {
            int middle = (start + end) / 2;

            mergeSort(array, start, middle, ordenProspero);
            mergeSort(array, middle + 1, end, ordenProspero);

            merge(array, start, middle, end, ordenProspero);
        }
    }

    private void merge(int[] array, int start, int middle, int end, boolean ordenProspero) {
        int size = end - start + 1;
        int[] res = new int[size];

        int i = start;
        int j = middle + 1;
        int k = 0;

        while ((i <= middle) && (j <= end)) {
            int idEstacion1 = array[i];
            int idEstacion2 = array[j];
            int demanda1 = this.estaciones.get(idEstacion1).getDemanda();
            int demanda2 = this.estaciones.get(idEstacion2).getDemanda();
            int bicisDisponibles1 = this.estaciones.get(idEstacion1).getNumBicicletasNext();
            int bicisDisponibles2 = this.estaciones.get(idEstacion2).getNumBicicletasNext();

            int evaluador1;
            int evaluador2;
            if (ordenProspero) {
                evaluador1 = bicisDisponibles1 - demanda1;
                evaluador2 = bicisDisponibles2 - demanda2;
            } else {
                evaluador1 = demanda1 - bicisDisponibles1;
                evaluador2 = demanda2 - bicisDisponibles2;
            }

            if (evaluador1 >= evaluador2) {
                res[k] = array[i];
                ++i;
            } else {
                res[k] = array[j];
                ++j;
            }

            ++k;
        }

        while (i <= middle) {
            res[k] = array[i];
            ++i;
            ++k;
        }

        while (j <= end) {
            res[k] = array[j];
            ++j;
            ++k;
        }

        for (k = 0; k < size; ++k) {
            array[start + k] = res[k];
        }
    }

    private int[] obtenerEstacionesConDeficit() { // O(|E|)
        ArrayList<Integer> arrayList = new ArrayList<Integer>();

        for (int i = 0; i < this.estaciones.size(); ++i) {
            int demanda = this.estaciones.get(i).getDemanda();
            int bicisDisponibles = this.estaciones.get(i).getNumBicicletasNext();

            if (demanda > bicisDisponibles) { // tiene deficit
                arrayList.add(i);
            }
        }

        return arrayList.stream().mapToInt(i -> i).toArray();
    }

    private int[] inicializarArrayBicisFaltantes(int[] estacionesConDeficit, int[] numBicisFaltantes) {
        System.out.println("Inicializando array de bicis faltantes:");
        for (int i = 0; i < estacionesConDeficit.length; ++i) {
            int demanda = this.estaciones.get(estacionesConDeficit[i]).getDemanda();
            int bicisDisponibles = this.estaciones.get(estacionesConDeficit[i]).getNumBicicletasNext();

            numBicisFaltantes[i] = demanda - bicisDisponibles;
            System.out.println(String.format("indice '%s' => num bicis faltantes = %s", i, numBicisFaltantes[i]));
        }

        return numBicisFaltantes;
    }*/
}