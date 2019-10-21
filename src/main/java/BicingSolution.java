import IA.Bicing.Estacion;
import IA.Bicing.Estaciones;

import java.util.ArrayList;
import java.util.Random;

public class BicingSolution {
    private static final int NUM_MAX_BICIS_FURGONETA = 30;

    private static Estaciones estaciones;       // ArrayList de objetos tipo Estacion del problema

    // ------------------------------------------------------------------------
    // Representación de la solución | Coste en memoria: O(4*|F|)
    // ------------------------------------------------------------------------
    private int[] asignaciones;                 // i->id furgoneta, [i]->id estacion [Memoria O(|F|)]

    private int[] primerosDestinos;             // i->id furgoneta, [i]->id estacion destino     [Memoria O(|F|)]
    private int[] segundosDestinos;             // i->id furgoneta, [i]->id estacion destino     [Memoria O(|F|)]

    private int[] primerasBicisDejadas;         // i->id furgoneta, [i]->número de bicis dejadas [Memoria O(|F|)]
    private int[] segundasBicisDejadas;         // i->id furgoneta, [i]->número de bicis dejadas [Memoria O(|F|)]

    private double costeTransporte;             // Coste por transporte al final de la hora
    private int beneficios;                     // Beneficios en euros obtenidos al final de la hora
    // (se le restan los costes por fallo)

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

        this.beneficios = 0;
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

        this.beneficios = new Integer(solution.getBeneficios());
        this.costeTransporte = new Double(solution.getCosteTransporte());
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

    // ------------------------------------------------------------------------
    // Operadores
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
            } else { // en caso de ya tener asignada una posicion de origen
                int cargaFurgoneta = this.primerasBicisDejadas[idFurgoneta] + this.segundasBicisDejadas[idFurgoneta];
                deshacerCalculoCosteTransporte(idFurgoneta);
                recalcularCostePorFallos(idFurgoneta, cargaFurgoneta, idEstacionFinal);
                this.asignaciones[idFurgoneta] = idEstacionFinal;
                calcularCosteTransporte(idFurgoneta);
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
     * @param destinoActual          destino a cambiar: 0 para el primer destino, 1 para el segundo destino
     * @param idEstacionDestinoFinal id de la estación destino final
     */
    public boolean cambiarEstacionDestino(int idFurgoneta, int destinoActual, int idEstacionDestinoFinal) { // TODO: Refactor code
        if (puedeCambiarEstacionDestino(idFurgoneta, destinoActual, idEstacionDestinoFinal)) {
            // HAPPY PATH
            int numBicisDemandadasDestino = 0;
            int numBicisDisponiblesDestino = 0;
            int carga = 0;
            if (destinoActual == 0) {
                deshacerCalculoCosteTransporte(idFurgoneta);
                if (this.primerosDestinos[idFurgoneta] != -1) {
                    numBicisDemandadasDestino = this.estaciones.get(this.primerosDestinos[idFurgoneta]).getDemanda();
                    numBicisDisponiblesDestino = this.estaciones.get(this.primerosDestinos[idFurgoneta]).getNumBicicletasNext();
                    carga = this.primerasBicisDejadas[idFurgoneta];
                    deshacerBeneficiosPorAciertos(numBicisDemandadasDestino, numBicisDisponiblesDestino, carga);
                }
                this.primerosDestinos[idFurgoneta] = idEstacionDestinoFinal;
            } else {
                deshacerCalculoCosteTransporte(idFurgoneta);
                if (this.segundosDestinos[idFurgoneta] != -1) {
                    numBicisDemandadasDestino = this.estaciones.get(this.segundosDestinos[idFurgoneta]).getDemanda();
                    numBicisDisponiblesDestino = this.estaciones.get(this.segundosDestinos[idFurgoneta]).getNumBicicletasNext();
                    carga = this.segundasBicisDejadas[idFurgoneta];
                    deshacerBeneficiosPorAciertos(numBicisDemandadasDestino, numBicisDisponiblesDestino, carga);
                }
                this.segundosDestinos[idFurgoneta] = idEstacionDestinoFinal;
            }

            if (carga > 0) {
                numBicisDemandadasDestino = this.estaciones.get(idEstacionDestinoFinal).getDemanda();
                numBicisDisponiblesDestino = this.estaciones.get(idEstacionDestinoFinal).getNumBicicletasNext();
                obtenerBeneficiosPorAciertos(numBicisDemandadasDestino, numBicisDisponiblesDestino, carga);
            }

            calcularCosteTransporte(idFurgoneta);

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
            int cargaFurgoneta1 = this.primerasBicisDejadas[idFurgoneta1] + this.segundasBicisDejadas[idFurgoneta1];
            int cargaFurgoneta2 = this.primerasBicisDejadas[idFurgoneta2] + this.segundasBicisDejadas[idFurgoneta2];

            int idEstacionFurgoneta1 = this.asignaciones[idFurgoneta1];
            int idEstacionFurgoneta2 = this.asignaciones[idFurgoneta2];

            if ((idEstacionFurgoneta1 == -1) && (idEstacionFurgoneta2 != -1)) {
                deshacerCalculoCosteTransporte(idFurgoneta2);
                deshacerCalculoCostePorFallos(idFurgoneta2, cargaFurgoneta2);

                this.primerosDestinos[idFurgoneta2] = -1;
                this.segundosDestinos[idFurgoneta2] = -1;
                this.primerasBicisDejadas[idFurgoneta2] = 0;
                this.segundasBicisDejadas[idFurgoneta2] = 0;

                this.asignaciones[idFurgoneta1] = idEstacionFurgoneta2;
                this.asignaciones[idFurgoneta2] = idEstacionFurgoneta1;

            } else if ((idEstacionFurgoneta1 != -1) && (idEstacionFurgoneta2 == -1)) {
                deshacerCalculoCosteTransporte(idFurgoneta1);
                deshacerCalculoCostePorFallos(idFurgoneta1, cargaFurgoneta1);

                this.primerosDestinos[idFurgoneta1] = -1;
                this.segundosDestinos[idFurgoneta1] = -1;
                this.primerasBicisDejadas[idFurgoneta1] = 0;
                this.segundasBicisDejadas[idFurgoneta1] = 0;

                this.asignaciones[idFurgoneta1] = idEstacionFurgoneta2;
                this.asignaciones[idFurgoneta2] = idEstacionFurgoneta1;

            } else { // Ambas estaciones tienen una estacion origen asignada
                deshacerCalculoCosteTransporte(idFurgoneta1);
                deshacerCalculoCosteTransporte(idFurgoneta2);

                recalcularCostePorFallos(idFurgoneta1, cargaFurgoneta1, idEstacionFurgoneta2);
                recalcularCostePorFallos(idFurgoneta2, cargaFurgoneta2, idEstacionFurgoneta1);

                this.asignaciones[idFurgoneta1] = idEstacionFurgoneta2;
                this.asignaciones[idFurgoneta2] = idEstacionFurgoneta1;

                calcularCosteTransporte(idFurgoneta1);
                calcularCosteTransporte(idFurgoneta2);
            }

            return true;
        }

        return false;
    }

    /**
     * Cargar furgoneta con id 'idFurgoneta' con 'numBicis' bicis en su numero de destino 'destinoActual'
     * <p>
     * Factor ramificación: O(31 * F * F)
     *
     * @param idFurgoneta id de la furgoneta a la que cargar las bicis
     * @param numBicis1   número de bicis que cargar en el destino1
     * @param numBicis2   numero de bicis que cargar en el destino2
     */
    public boolean cargarFurgoneta(int idFurgoneta, int numBicis1, int numBicis2) { // si asigna 0 a un destino, eliminarlo
        if (puedeCargarFurgoneta(idFurgoneta, numBicis1, numBicis2)) { // A partir de aqui ya no existen sucesores sin destinos
            int bicisDemandadasOrigen = this.estaciones.get(this.asignaciones[idFurgoneta]).getDemanda();
            int bicisDisponiblesOrigen = this.estaciones.get(this.asignaciones[idFurgoneta]).getNumBicicletasNext();
            int cargaTotalBicis;
            if ((this.primerosDestinos[idFurgoneta] != -1) && (this.segundosDestinos[idFurgoneta] == -1)) { // Tiene asignado solo destino1
                cargaTotalBicis = numBicis1;
                int bicisDemandadasDestino1 = this.estaciones.get(this.primerosDestinos[idFurgoneta]).getDemanda();
                int bicisDisponiblesDestino1 = this.estaciones.get(this.primerosDestinos[idFurgoneta]).getNumBicicletasNext();

                deshacerCalculoCosteTransporte(idFurgoneta);
                deshacerCalculoCostePorFallos(idFurgoneta, this.primerasBicisDejadas[idFurgoneta]);
                deshacerBeneficiosPorAciertos(bicisDemandadasDestino1, bicisDisponiblesDestino1, this.primerasBicisDejadas[idFurgoneta]);

                this.primerasBicisDejadas[idFurgoneta] = cargaTotalBicis;

                obtenerBeneficiosPorAciertos(bicisDemandadasDestino1, bicisDisponiblesDestino1, cargaTotalBicis);
                penalizarCostePorFallos(bicisDemandadasOrigen, bicisDisponiblesOrigen, cargaTotalBicis);
                calcularCosteTransporte(idFurgoneta);
            } else if ((this.primerosDestinos[idFurgoneta] == -1) && (this.segundosDestinos[idFurgoneta] != -1)) { // Tiene asignado solo destino2
                // TODO: tiene sentido??? destino2 no puede ser != -1 si destino1 es == -1
                cargaTotalBicis = numBicis2;
                int bicisDemandadasDestino2 = this.estaciones.get(this.segundosDestinos[idFurgoneta]).getDemanda();
                int bicisDisponiblesDestino2 = this.estaciones.get(this.segundosDestinos[idFurgoneta]).getNumBicicletasNext();

                deshacerCalculoCosteTransporte(idFurgoneta);
                deshacerCalculoCostePorFallos(idFurgoneta, this.segundasBicisDejadas[idFurgoneta]);
                deshacerBeneficiosPorAciertos(bicisDemandadasDestino2, bicisDisponiblesDestino2, this.segundasBicisDejadas[idFurgoneta]);

                this.segundasBicisDejadas[idFurgoneta] = cargaTotalBicis;

                obtenerBeneficiosPorAciertos(bicisDemandadasDestino2, bicisDisponiblesDestino2, cargaTotalBicis);
                penalizarCostePorFallos(bicisDemandadasOrigen, bicisDisponiblesOrigen, cargaTotalBicis);
                calcularCosteTransporte(idFurgoneta);
            } else { // Tiene dos destinos asignados
                cargaTotalBicis = numBicis1 + numBicis2;
                int bicisDemandadasDestino1 = this.estaciones.get(this.primerosDestinos[idFurgoneta]).getDemanda();
                int bicisDisponiblesDestino1 = this.estaciones.get(this.primerosDestinos[idFurgoneta]).getNumBicicletasNext();
                int bicisDemandadasDestino2 = this.estaciones.get(this.segundosDestinos[idFurgoneta]).getDemanda();
                int bicisDisponiblesDestino2 = this.estaciones.get(this.segundosDestinos[idFurgoneta]).getNumBicicletasNext();

                deshacerCalculoCosteTransporte(idFurgoneta);
                deshacerCalculoCostePorFallos(idFurgoneta, this.primerasBicisDejadas[idFurgoneta]);
                deshacerBeneficiosPorAciertos(bicisDemandadasDestino1, bicisDisponiblesDestino1, this.primerasBicisDejadas[idFurgoneta]);
                deshacerBeneficiosPorAciertos(bicisDemandadasDestino2, bicisDisponiblesDestino2, this.segundasBicisDejadas[idFurgoneta]);

                this.primerasBicisDejadas[idFurgoneta] = numBicis1;
                this.segundasBicisDejadas[idFurgoneta] = numBicis2;

                obtenerBeneficiosPorAciertos(bicisDemandadasDestino1, bicisDisponiblesDestino1, numBicis1);
                obtenerBeneficiosPorAciertos(bicisDemandadasDestino2, bicisDisponiblesDestino2, numBicis2);
                penalizarCostePorFallos(bicisDemandadasOrigen, bicisDisponiblesOrigen, cargaTotalBicis);
                calcularCosteTransporte(idFurgoneta);
            }

            return true;
        }

        return false;
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

    public int getBeneficios() {
        return this.beneficios;
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
            estacionesAsignadas[idFurgoneta] = true;
        } else {
            idEstacionRandom = -1;
        }

        this.asignaciones[idFurgoneta] = idEstacionRandom;
        System.out.println(String.format("Asignada la estacion origen random '%s'", this.asignaciones[idFurgoneta]));

        return estacionesAsignadas;
    }

    private void asignarFurgonetaEnEstacionProspera(int idFurgoneta, int[] estacionesProsperasAsignadas) {
        System.out.println(String.format("Asignando a furgoneta con id '%s' la estacion con id '%s'",
                idFurgoneta, estacionesProsperasAsignadas[idFurgoneta]));

        this.asignaciones[idFurgoneta] = estacionesProsperasAsignadas[idFurgoneta];

        System.out.println(String.format("Asignada a furgoneta con id '%s' la estacion con id '%s'",
                idFurgoneta, this.asignaciones[idFurgoneta]));
    }

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

    private void asignarCargaDestinos(int idFurgoneta, Random random) { // O(1)
        int idEstacionOrigen = this.asignaciones[idFurgoneta];
        if (idEstacionOrigen != -1) {
            int numBicisDemandadasEstacionOrigen = this.estaciones.get(idEstacionOrigen).getDemanda();
            int numBicisDisponiblesEstacionOrigen = this.estaciones.get(idEstacionOrigen).getNumBicicletasNext();
            int idEstacionDestino1 = this.primerosDestinos[idFurgoneta];
            if (idEstacionDestino1 != -1) {
                int numBicisDemandadasDestino = this.estaciones.get(idEstacionDestino1).getDemanda();
                int numBicisDisponiblesDestino = this.estaciones.get(idEstacionDestino1).getNumBicicletasNext();
                int bicisDisponiblesParaCargar = numBicisDisponiblesEstacionOrigen;
                if (bicisDisponiblesParaCargar > NUM_MAX_BICIS_FURGONETA) {
                    bicisDisponiblesParaCargar = 30;
                }
                System.out.println(String.format("Asignando a la furgoneta con id '%s' en la estacion de origen con id " +
                                "'%s', una carga entre 0 y '%s' bicis para el destino1",
                        idFurgoneta, idEstacionOrigen, bicisDisponiblesParaCargar));
                int cargaRandom = random.nextInt(bicisDisponiblesParaCargar);
                this.primerasBicisDejadas[idFurgoneta] = cargaRandom;
                System.out.println(String.format("Asignadas '%s' bicis al destino1",
                        this.primerasBicisDejadas[idFurgoneta]));

                if (cargaRandom == 0) {
                    this.primerosDestinos[idFurgoneta] = -1;
                } else {
                    obtenerBeneficiosPorAciertos(numBicisDemandadasDestino, numBicisDisponiblesDestino, cargaRandom);

                    numBicisDisponiblesEstacionOrigen = penalizarCostePorFallos(numBicisDemandadasEstacionOrigen,
                            numBicisDisponiblesEstacionOrigen, cargaRandom);
                }

                int idEstacionDestino2 = this.segundosDestinos[idFurgoneta];
                if (idEstacionDestino2 != -1) {
                    numBicisDemandadasDestino = this.estaciones.get(idEstacionDestino2).getDemanda();
                    numBicisDisponiblesDestino = this.estaciones.get(idEstacionDestino2).getNumBicicletasNext();
                    bicisDisponiblesParaCargar -= cargaRandom;
                    System.out.println(String.format("Asignando una carga entre 0 y '%s'", bicisDisponiblesParaCargar));
                    if (bicisDisponiblesParaCargar > 0) {
                        cargaRandom = random.nextInt(bicisDisponiblesParaCargar);
                        this.segundasBicisDejadas[idFurgoneta] = cargaRandom;
                    } else {
                        cargaRandom = 0;
                        this.segundasBicisDejadas[idFurgoneta] = 0;
                    }
                    System.out.println(String.format("Asignadas '%s' bicis al destino2",
                            this.segundasBicisDejadas[idFurgoneta]));
                    if (cargaRandom == 0) {
                        this.segundosDestinos[idFurgoneta] = -1;
                    } else {
                        obtenerBeneficiosPorAciertos(numBicisDemandadasDestino, numBicisDisponiblesDestino, cargaRandom);
                        penalizarCostePorFallos(numBicisDemandadasEstacionOrigen, numBicisDisponiblesEstacionOrigen,
                                cargaRandom);
                    }
                } else {
                    System.out.println("No tiene un segundo destino");
                    this.segundasBicisDejadas[idFurgoneta] = 0;
                }
            } else { // Si el destino1 es -1, entonces el destino2 sera tambien -1
                System.out.println(String.format("Furgoneta con id '%s' sin destinos", idFurgoneta));
                this.primerasBicisDejadas[idFurgoneta] = 0;
                this.segundasBicisDejadas[idFurgoneta] = 0;
            }

            if ((this.primerosDestinos[idFurgoneta] == -1) && (this.segundosDestinos[idFurgoneta] != -1)) {
                this.primerosDestinos[idFurgoneta] = this.segundosDestinos[idFurgoneta];
                this.segundosDestinos[idFurgoneta] = -1;
            }
        }
    }

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

    private int penalizarCostePorFallos(int numBicisDemandadasEstacionOrigen, int numBicisDisponiblesEstacionOrigen,
                                        int cargaRandom) {      // O(1)
        System.out.println("Penalizamos fallos");
        boolean existeDeficitAntesDeCargar = (numBicisDemandadasEstacionOrigen > numBicisDisponiblesEstacionOrigen);
        boolean existeDeficitDespuesDeCargar = (numBicisDemandadasEstacionOrigen > numBicisDisponiblesEstacionOrigen - cargaRandom);
        System.out.println(String.format("Num bicis demandadas = '%s'", numBicisDemandadasEstacionOrigen));
        System.out.println(String.format("Num bicis disponibles = '%s'", numBicisDisponiblesEstacionOrigen));
        if (existeDeficitAntesDeCargar) {
            this.beneficios -= cargaRandom;
            numBicisDisponiblesEstacionOrigen -= cargaRandom;
            System.out.println(String.format("Coste por fallo = '%s'", cargaRandom));
        } else if (existeDeficitDespuesDeCargar) {
            this.beneficios -= (numBicisDemandadasEstacionOrigen - numBicisDisponiblesEstacionOrigen + cargaRandom);
            System.out.println(String.format("Coste por fallo = '%s'",
                    (numBicisDemandadasEstacionOrigen - numBicisDisponiblesEstacionOrigen + cargaRandom)));
        } else {
            System.out.println("No hay coste por fallos");
        }

        return numBicisDisponiblesEstacionOrigen;
    }

    private void obtenerBeneficiosPorAciertos(int numBicisDemandadasDestino, int numBicisDisponiblesDestino,
                                              int cargaRandom) {        // O(1)
        System.out.println("Obtenemos beneficios por aciertos");
        boolean existeDeficitAntesDeDescargar = (numBicisDemandadasDestino > numBicisDisponiblesDestino);
        boolean existeDeficitDespuesDeDescargar = (numBicisDemandadasDestino > numBicisDisponiblesDestino + cargaRandom);
        System.out.println(String.format("Num bicis demandadas = '%s'", numBicisDemandadasDestino));
        System.out.println(String.format("Num bicis disponibles = '%s'", numBicisDisponiblesDestino));
        if (existeDeficitAntesDeDescargar && existeDeficitDespuesDeDescargar) {
            this.beneficios += cargaRandom;
            System.out.println(String.format("Beneficio por acierto = '%s'", cargaRandom));
        } else if (existeDeficitAntesDeDescargar && !(existeDeficitDespuesDeDescargar)) {
            this.beneficios += (numBicisDemandadasDestino - numBicisDisponiblesDestino);
            System.out.println(String.format("Beneficio por acierto = '%s'",
                    (numBicisDemandadasDestino - numBicisDisponiblesDestino)));
        } else {
            System.out.println("No hay beneficios obtenidos");
        }
    }

    private void deshacerBeneficiosPorAciertos(int numBicisDemandadasDestino, int numBicisDisponiblesDestino,
                                               int carga) {
        boolean existeDeficitAntesDeDescargar = (numBicisDemandadasDestino > numBicisDisponiblesDestino);
        boolean existeDeficitDespuesDeDescargar = (numBicisDemandadasDestino > numBicisDisponiblesDestino + carga);

        if (existeDeficitAntesDeDescargar && existeDeficitDespuesDeDescargar) {
            this.beneficios -= carga;
        } else if (existeDeficitAntesDeDescargar && !(existeDeficitDespuesDeDescargar)) {
            this.beneficios -= (numBicisDemandadasDestino - numBicisDisponiblesDestino);
        }
    }

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

    // Si la estacion final es la misma que la actual, devuelve false
    // Si la estacion final no tiene bicis disponibles que la carga actual, devuelve false
    // otherwise devuelve true
    private boolean puedeMoverFurgoneta(int idFurgoneta, int idEstacionFinal) {
        int idEstacionOrigenActual = this.asignaciones[idFurgoneta];
        int cargaFurgoneta = this.primerasBicisDejadas[idFurgoneta] + this.segundasBicisDejadas[idFurgoneta];
        int bicisDisponiblesEstacionFinal = this.estaciones.get(idEstacionFinal).getNumBicicletasNext();

        return ((idEstacionFinal != idEstacionOrigenActual) && (cargaFurgoneta <= bicisDisponiblesEstacionFinal));
    }

    private boolean puedeCambiarEstacionDestino(int idFurgoneta, int destinoActual, int idEstacionDestinoFinal) {
        int idEstacionOrigen = this.asignaciones[idFurgoneta];

        if (idEstacionOrigen == -1) {
            return false;
        }

        if (idEstacionOrigen == idEstacionDestinoFinal) {
            return false;
        }

        if ((this.primerosDestinos[idFurgoneta] != -1) && (this.segundosDestinos[idFurgoneta] != -1)) {
            if ((destinoActual == 0) && (idEstacionDestinoFinal == this.segundosDestinos[idFurgoneta])) {
                return false;
            }

            if ((destinoActual == 1) && (idEstacionDestinoFinal == this.primerosDestinos[idFurgoneta])) {
                return false;
            }
        }

        return true;
    }

    private boolean puedeIntercambiarFurgonetas(int idFurgoneta1, int idFurgoneta2) {
        int idEstacionFurgoneta1 = this.asignaciones[idFurgoneta1];
        int idEstacionFurgoneta2 = this.asignaciones[idFurgoneta2];

        if (idEstacionFurgoneta1 == -1 && idEstacionFurgoneta2 == -1) {
            return false;
        } else if (idEstacionFurgoneta1 == -1 || idEstacionFurgoneta2 == -1) {
            return true;
        } else {
            int cargaFurgoneta1 = this.primerasBicisDejadas[idFurgoneta1] + this.segundasBicisDejadas[idFurgoneta1];
            int cargaFurgoneta2 = this.primerasBicisDejadas[idFurgoneta2] + this.segundasBicisDejadas[idFurgoneta2];
            int bicisDisponiblesEstacionFinalFurgoneta1 = this.estaciones.get(idEstacionFurgoneta2).getNumBicicletasNext();
            int bicisDisponiblesEstacionFinalFurgoneta2 = this.estaciones.get(idEstacionFurgoneta1).getNumBicicletasNext();

            return ((cargaFurgoneta1 <= bicisDisponiblesEstacionFinalFurgoneta1) && (cargaFurgoneta2 <= bicisDisponiblesEstacionFinalFurgoneta2));
        }
    }

    private boolean puedeCargarFurgoneta(int idFurgoneta, int numBicis1, int numBicis2) {
        if ((this.primerosDestinos[idFurgoneta] == -1) && (this.segundosDestinos[idFurgoneta] == -1)) {
            return false;
        }

        int cargaTotalBicis = numBicis1 + numBicis2;

        if ((cargaTotalBicis > 30) || (this.estaciones.get(this.asignaciones[idFurgoneta]).getNumBicicletasNext() < cargaTotalBicis)) {
            return false;
        }

        return true;
    }

    private void recalcularCostePorFallos(int idFurgoneta, int cargaFurgoneta, int idEstacionFinal) { // TODO: Refactor code
        // Devolver beneficios de la estacion actual
        Estacion estacion = this.estaciones.get(this.asignaciones[idFurgoneta]);
        int demandaEstacion = estacion.getDemanda();
        int bicisLibres = estacion.getNumBicicletasNext();

        if (demandaEstacion > bicisLibres) {
            this.beneficios += cargaFurgoneta;
        } else if (demandaEstacion > bicisLibres - cargaFurgoneta) {
            this.beneficios += (demandaEstacion - bicisLibres + cargaFurgoneta);
        } // else: no hubo penalizacion por fallo

        // Calcular beneficios de la estacion final
        estacion = this.estaciones.get(idEstacionFinal);
        demandaEstacion = estacion.getDemanda();
        bicisLibres = estacion.getNumBicicletasNext();

        penalizarCostePorFallos(demandaEstacion, bicisLibres, cargaFurgoneta);
    }

    private void deshacerCalculoCostePorFallos(int idFurgoneta, int cargaFurgoneta) { // TODO: Refactor code
        Estacion estacion = this.estaciones.get(this.asignaciones[idFurgoneta]);
        int demandaEstacion = estacion.getDemanda();
        int bicisLibres = estacion.getNumBicicletasNext();

        if (demandaEstacion > bicisLibres) {
            this.beneficios += cargaFurgoneta;
        } else if (demandaEstacion > bicisLibres - cargaFurgoneta) {
            this.beneficios += (demandaEstacion - bicisLibres + cargaFurgoneta);
        } // else: no hubo penalizacion por fallo
    }

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
        System.arraycopy(solution.getPrimerosDestinos(), 0, this.primerosDestinos, 0, solution.getPrimerosDestinos().length);
        System.arraycopy(solution.getSegundosDestinos(), 0, this.segundosDestinos, 0, solution.getSegundosDestinos().length);
        System.arraycopy(solution.getPrimerasBicisDejadas(), 0, this.primerasBicisDejadas, 0, solution.getPrimerasBicisDejadas().length);
        System.arraycopy(solution.getSegundasBicisDejadas(), 0, this.segundasBicisDejadas, 0, solution.getSegundasBicisDejadas().length);
    }

    private void mergeSort(int[] array, int start, int end, boolean ordenProspero) { // O(nlogn)
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
    }
}