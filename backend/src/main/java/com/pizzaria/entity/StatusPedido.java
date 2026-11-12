package com.pizzaria.entity;

public enum StatusPedido {
    REGISTRADO,
    EM_PREPARO,
    PRONTO,
    ENTREGUE;

    public boolean permiteAvancarPara(StatusPedido novoStatus) {
        return novoStatus.ordinal() == ordinal() + 1;
    }
}
