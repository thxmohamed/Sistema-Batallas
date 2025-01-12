import axios from "axios";

const sistemaBatallas = "localhost:8090";

export default axios.create({
    baseURL: `http://${sistemaBatallas}`,
    headers: {
        'Content-Type': 'application/json'
    } 
});
