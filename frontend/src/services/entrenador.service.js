import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get('/entrenador/')
}

const getById = id => {
    return httpClient.get(`/entrenador/${id}`)
}

const create = (data) => {
    return httpClient.post("/entrenador/create", data);
}

const getPokemon = id => {
    return httpClient.get(`/entrenador/pokemons/${id}`)
}

export default {
    getAll,
    getById, 
    create, 
    getPokemon
}