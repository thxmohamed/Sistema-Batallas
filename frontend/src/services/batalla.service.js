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

const combatir = (posicionAtacante, posicionReceptor, data) => {
    return httpClient.post(`/batalla/combate/${posicionAtacante}/${posicionReceptor}`, data)
}

const createRandomBattle = () => {
    return httpClient.post('/batalla/batalla-aleatoria')
}

export default {
    getAll,
    getById,
    create,
    combatir,
    createRandomBattle
}