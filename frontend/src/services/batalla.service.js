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

const createRandomBattleWithMode = (mode) => {
    return httpClient.post(`/batalla/batalla-aleatoria/${mode}`)
}

const createCpuHardBattle = (equipoHumano) => {
    return httpClient.post('/batalla/batalla-cpu-hard', equipoHumano)
}

const createBattleWithDifficulty = (mode, difficulty, equipoHumano = null) => {
    return httpClient.post(`/batalla/batalla-dificultad/${mode}/${difficulty}`, equipoHumano)
}

export default {
    getAll,
    getById,
    create,
    combatir,
    createRandomBattle,
    createRandomBattleWithMode,
    createCpuHardBattle,
    createBattleWithDifficulty
}