import httpClient from "../http-common";

/**
 * Servicio para manejar las acciones de la CPU
 */

/**
 * Obtiene la próxima acción que debe realizar la CPU
 * @param {Object} batalla - Estado actual de la batalla
 * @param {string} difficulty - Dificultad de la CPU (EASY, NORMAL, HARD)
 * @param {boolean} cpuIsTeam1 - Si la CPU es el entrenador 1
 * @returns {Promise} Respuesta con la acción de la CPU
 */
const getCpuAction = (batalla, difficulty = 'NORMAL', cpuIsTeam1 = false) => {
    const request = {
        batalla: batalla,
        difficulty: difficulty,
        cpuIsTeam1: cpuIsTeam1
    };
    
    console.log("CPU Service - Enviando request:", request);
    
    return httpClient.post('/cpu/action', request).then(response => {
        console.log("CPU Service - Respuesta recibida:", response);
        return response;
    }).catch(error => {
        console.error("CPU Service - Error:", error);
        throw error;
    });
};

/**
 * Verifica que el controlador CPU está funcionando
 * @returns {Promise} Respuesta de prueba
 */
const testCpuController = () => {
    return httpClient.get('/cpu/test');
};

export default {
    getCpuAction,
    testCpuController
};
