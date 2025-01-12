import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get('/batalla/')
}

const getById = id => {
    return httpClient.get(`/batalla/${id}`)
}

const create = data => {
    return httpClient.post("/batalla/create", data)
}

const combatir = data => {
    return httpClient.post(`/batalla/combate/${posAtacanteE1}/${posAtacanteE2}/${posAgredidoE1}/${posAgredidoE2}`, data)
}

export default {
    getAll,
    getById,
    create,
    combatir
}