package com.example.demo.dto;

public class WebhookPayloadDTO {
    private Integer idPedido;

    public WebhookPayloadDTO() {}
    public Integer getIdPedido()           { return idPedido; }
    public void    setIdPedido(Integer v)  { this.idPedido = v; }
}
