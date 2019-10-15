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
                Random random = new Random();                       // TODO: eliminar
                this.asignaciones[idFurgoneta] = idEstacionFinal;
                asignarDestinos(idFurgoneta, random);               // TODO: cambiar a ningun destino (obtiene destinos mediante operador)
                asignarCargaDestinos(idFurgoneta, random);          // TODO: cambiar a ninguna carga (obtiene carga mediante operador)
                calcularCosteTransporte(idFurgoneta);               // TODO: eliminar
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

    // TODO: Discutir que hacer con el tema de las bicis ????
//    /**
//     * Cargar furgoneta con id 'idFurgoneta' con 'numBicis' bicis
//     * <p>
//     * Factor ramificación: O(31 * F * F)
//     *
//     * @param idFurgoneta id de la furgoneta a la que cargar las bicis
//     * @param numBicis    número de bicis que cargar
//     */
//    public void cargarFurgoneta(int idFurgoneta, int numBicis) {
//        // Empty
//    }

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

    private void copyArraysFrom(BicingSolution solution) { // O(1)
        System.arraycopy(solution.getAsignaciones(), 0, this.asignaciones, 0, solution.getAsignaciones().length);
        System.arraycopy(solution.getPrimerosDestinos(), 0, this.primerosDestinos, 0, solution.getPrimerosDestinos().length);
        System.arraycopy(solution.getSegundosDestinos(), 0, this.segundosDestinos, 0, solution.getSegundosDestinos().length);
        System.arraycopy(solution.getPrimerasBicisDejadas(), 0, this.primerasBicisDejadas, 0, solution.getPrimerasBicisDejadas().length);
        System.arraycopy(solution.getSegundasBicisDejadas(), 0, this.segundasBicisDejadas, 0, solution.getSegundasBicisDejadas().length);
    }
}