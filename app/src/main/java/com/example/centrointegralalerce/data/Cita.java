package com.example.centrointegralalerce.data;

public class Cita {
    private String actividad;
    private String lugar;
    private String fechaHora; // Formato: "Lun 09:00"
    private String tipoActividad;
    private int diaSemana; // 0=Lun, 1=Mar, ..., 6=Dom
    private String hora; // "09:00"

    public Cita(String actividad, String lugar, String fechaHora, String tipoActividad) {
        this.actividad = actividad;
        this.lugar = lugar;
        this.fechaHora = fechaHora;
        this.tipoActividad = tipoActividad;
        parseFechaHora();
    }

    private void parseFechaHora() {
        if (fechaHora != null && fechaHora.length() >= 8) {
            String dia = fechaHora.substring(0, 3);
            this.hora = fechaHora.substring(4);

            switch (dia) {
                case "Lun": this.diaSemana = 0; break;
                case "Mar": this.diaSemana = 1; break;
                case "Mié": this.diaSemana = 2; break;
                case "Jue": this.diaSemana = 3; break;
                case "Vie": this.diaSemana = 4; break;
                case "Sáb": this.diaSemana = 5; break;
                case "Dom": this.diaSemana = 6; break;
                default: this.diaSemana = 0;
            }
        }
    }

    // Getters
    public String getActividad() { return actividad; }
    public String getLugar() { return lugar; }
    public String getFechaHora() { return fechaHora; }
    public String getTipoActividad() { return tipoActividad; }
    public int getDiaSemana() { return diaSemana; }
    public String getHora() { return hora; }
}