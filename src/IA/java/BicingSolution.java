package IA.java;

import IA.Bicing.Estacion;
import IA.Bicing.Estaciones;

import java.util.Random;

public class BicingSolution {
    // ------------------------------------------------------------------------
    // Atributos de la solucion
    // ------------------------------------------------------------------------
    private static final int NUM_MAX_BICIS_FURGONETA = 30;
    private static final int NUM_MAX_DESTINOS = 2;

    private static Estaciones estaciones;       // ArrayList de objetos tipo Estacion del problema
    private static int numEstaciones;           // Numero total de estaciones del problema
    private static int numBicisTotal;           // Numero total de bicis del problema
    private static int numFurgonetas;           // Numero total de furgonetas del problema

    private int[] asignaciones;                 // i->id furgoneta, [i]->id estacion
    private int[] capacidadEstaciones;          // i->id estacion,  [i]->num bicis total en estacion final hora
    private int[] capacidadFurgonetas;          // i->id furgoneta, [i]->num bicis total en furgoneta
    private int[][] destinos;                   // i->id furgoneta, [i][0]->destino1, [i][1]->destino2

    private double beneficios;                  // Beneficios en euros obtenidos al final de la hora
    private double coste;                       // Coste por fallos y por transporte al final de la hora


    // ------------------------------------------------------------------------
    // Constructores
    // ------------------------------------------------------------------------

    /**
     * Construye una solucion vacia
     *
     * @param numEstaciones Numero de estaciones del problema
     * @param numBicisTotal Numero de bicis del problema
     * @param tipoDemanda   Tipo de demanda de la hora actual
     * @param seed          Semilla aleatoria
     * @param numFurgonetas Numero de furgonetas del problema
     */
    public BicingSolution(int numEstaciones, int numBicisTotal, int tipoDemanda, int seed, int numFurgonetas) {
        this.estaciones = new Estaciones(numEstaciones, numBicisTotal, tipoDemanda, seed);

        this.numEstaciones = numEstaciones;
        this.numBicisTotal = numBicisTotal;
        this.numFurgonetas = numFurgonetas;

        inicializarAsignaciones();
        inicializarCapacidadEstaciones();
        inicializarCapacidadFurgonetas();
        inicializarDestinos();

        this.beneficios = 0.0;
        this.coste = 0.0;
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

    public int[] getCapacidadEstaciones() {
        return this.capacidadEstaciones;
    }

    public int[] getCapacidadFurgonetas() {
        return this.capacidadFurgonetas;
    }

    public int[][] getDestinos() {
        return this.destinos;
    }

    // ------------------------------------------------------------------------
    // Generadores de soluciones iniciales
    // ------------------------------------------------------------------------

    /**
     * Genera una solucion inicial random
     */
    public void generarSolucionInicialRandom() {
        Random random = new Random();

        boolean[] estacionesOcupadas = new boolean[this.numEstaciones];
        estacionesOcupadas = inicializarBoolArray(estacionesOcupadas);

        boolean[] estacionesVisitadas = new boolean[this.numEstaciones];
        estacionesVisitadas = inicializarBoolArray(estacionesVisitadas);

        for (int i = 0; i < this.numFurgonetas; ++i) {
            System.out.println("Furgoneta #" + i);
            System.out.println("Asignar estacion");
            int idEstacion = asignarEstacion(random, estacionesOcupadas, i);
            if (idEstacion != -1) {
                estacionesOcupadas[idEstacion] = true;
                System.out.println("Escoger num bicis por furgoneta + Cargar furgoneta");
                cargarFurgoneta(i, escogerNumBicisPorFurgoneta(random, i));
                System.out.println("Asignar destinos");
                estacionesVisitadas = asignarDestinos(random, estacionesVisitadas, i);
            }
        }

        System.out.println("Calcular beneficios y costes del problema");
        calcularBeneficiosYCostes();
        System.out.println("Beneficios totales del problema = " + this.beneficios);
        System.out.println("Coste total del problema = " + this.coste);
    }

    // ------------------------------------------------------------------------
    // Metodos privados auxiliares
    // ------------------------------------------------------------------------
    private void inicializarAsignaciones() {
        this.asignaciones = new int[this.numFurgonetas];

        for (int i = 0; i < this.numFurgonetas; ++i) {
            this.asignaciones[i] = -1;
        }
    }

    private void inicializarCapacidadEstaciones() {
        this.capacidadEstaciones = new int[this.numEstaciones];

        for (int i = 0; i < this.numEstaciones; ++i) {
            this.capacidadEstaciones[i] = this.estaciones.get(i).getNumBicicletasNext();
        }
    }

    private void inicializarCapacidadFurgonetas() {
        this.capacidadFurgonetas = new int[this.numFurgonetas];

        for (int i = 0; i < this.numFurgonetas; ++i) {
            this.capacidadFurgonetas[i] = 0;
        }
    }

    private void inicializarDestinos() {
        this.destinos = new int[this.numFurgonetas][2];

        for (int i = 0; i < this.numFurgonetas; ++i) {
            this.destinos[i][0] = -1;
            this.destinos[i][1] = -1;
        }
    }

    private boolean[] inicializarBoolArray(boolean[] array) {
        for (int i = 0; i < array.length; ++i) {
            array[i] = false;
        }

        return array;
    }

    private int asignarEstacion(Random random, boolean[] estacionesOcupadas, int idFurgoneta) {
        int idEstacion = random.nextInt(this.numEstaciones + 1);
        if (idEstacion == this.numEstaciones || estacionesOcupadas[idEstacion]) {
            idEstacion = -1;          // No se le asigna estacion, es decir, no se utilizara en la solucion
        }
        System.out.println("Estacion = " + idEstacion);
        this.asignaciones[idFurgoneta] = idEstacion;

        return idEstacion;
    }

    private int escogerNumBicisPorFurgoneta(Random random, int idFurgoneta) {
        int numBicisParaCargar = random.nextInt(this.estaciones.get(this.asignaciones[idFurgoneta])
                .getNumBicicletasNext() + 1);
        System.out.println("Num bicis para cargar = " + numBicisParaCargar);
        return Math.min(numBicisParaCargar, NUM_MAX_BICIS_FURGONETA);
    }

    private void cargarFurgoneta(int idFurgoneta, int numBicisParaCargar) {
        this.capacidadEstaciones[this.asignaciones[idFurgoneta]] -= numBicisParaCargar; // Cogemos bicis de la estacion
        this.capacidadFurgonetas[idFurgoneta] = numBicisParaCargar;                     // Cargar bicis en furgoneta
    }

    private boolean[] asignarDestinos(Random random, boolean[] estacionesVisitadas, int idFurgoneta) {
        for (int i = 0; i < NUM_MAX_DESTINOS; ++i) {
            int idEstacion = random.nextInt(this.numEstaciones + 1);

            if (idEstacion == this.numEstaciones || estacionesVisitadas[idEstacion]) {
                idEstacion = -1;    // No se le asigna destino
                System.out.println("Estacion = " + idEstacion);
            } else {
                System.out.println("Estacion = " + idEstacion);
                System.out.println("Escoger num bicis por estacion visitada + Descargar furgoneta");
                descargarFurgoneta(idFurgoneta, idEstacion, escogerNumBicisPorEstacionVisitada(random, idFurgoneta, idEstacion));
                estacionesVisitadas[idEstacion] = true;
            }

            this.destinos[idFurgoneta][i] = idEstacion;
        }

        if (this.destinos[idFurgoneta][0] == -1) {      // Organiza destinos en caso de ser nulo el primer destino
            this.destinos[idFurgoneta][0] = this.destinos[idFurgoneta][1];
            this.destinos[idFurgoneta][1] = -1;
        }

        return estacionesVisitadas;
    }

    private int escogerNumBicisPorEstacionVisitada(Random random, int idFurgoneta, int idEstacion) {
        int numBicisParaDescargar = random.nextInt(this.capacidadFurgonetas[idFurgoneta] + 1);
        System.out.println("Num bicis para descargar = " + numBicisParaDescargar);
        return numBicisParaDescargar;
    }

    private void descargarFurgoneta(int idFurgoneta, int idEstacion, int numBicisParaDescargar) {
        this.capacidadFurgonetas[idFurgoneta] -= numBicisParaDescargar;
        this.capacidadEstaciones[idEstacion] += numBicisParaDescargar;
    }

    private void calcularBeneficiosYCostes() {
        for (int i = 0; i < this.numEstaciones; ++i) {
            Estacion estacion = this.estaciones.get(i);
            int demanda = estacion.getDemanda();
            int numBicisInicioHora = estacion.getNumBicicletasNext();
            int numBicisFinalHora = this.capacidadEstaciones[i];

            if (demanda >= numBicisFinalHora) {
                if (numBicisFinalHora >= numBicisInicioHora) {
                    this.beneficios += (numBicisFinalHora - numBicisInicioHora);
                } else {
                    this.coste += (numBicisInicioHora - numBicisFinalHora);
                }
            } else {
                if (numBicisFinalHora > numBicisInicioHora && demanda > numBicisInicioHora) {
                    this.beneficios += (demanda - numBicisInicioHora);
                }
            }
        }
    }
}
