import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get('/pokemon/')
}

const getById = id => {
    return httpClient.get(`/pokemon/${id}`)
}

const create = (data) => {
    return httpClient.post("/pokemon/create", data, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  };

const downloadSprite = id => {
    return httpClient.get(`/pokemon/download/${id}`)
}

const getAtaques = id => {
    return httpClient.get(`/pokemon/ataques/${id}`)
}

const getEfecto = id => {
    return httpClient.get(`/pokemon/efecto/${id}`)
}

const atacar = data => {
    return httpClient.post("/pokemon/atacar", data)
}

const aplicarEfecto = data => {
    return httpClient.post("/pokemon/aplicarEfecto", data)
}

export default {
    getAll,
    getById, 
    create, 
    downloadSprite, 
    getAtaques, 
    getEfecto,
    atacar, 
    aplicarEfecto
}