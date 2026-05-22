const http = require('http');
const httpProxy = require('http-proxy');
const Redis = require('ioredis');

const redisUrl = process.env.REDIS_URL || 'redis://redis-service:6379';

const redis = new Redis(redisUrl,{
    maxRetriesPerRequest: null,
    enableReadyCheck: false,
    retryStrategy: (times) => {
        const delay = Math.min(times * 50, 2000); //Backoff strategy
        console.log(`Redis connection failed. Retrying in ${delay}ms...`);
        return delay;
    }
});

redis.on('error', (err) => {
    console.error('Redis Client error:', err);
});

redis.on('connect', () => {
    console.log('Connected to Redis successfully!');
});

const proxy = httpProxy.createProxyServer({
    ws: true,
    xfwd: true,
    changeOrigin: true
});

async function getTarget(hostname){

    try{
        const targetIp = await redis.get(`route:${hostname}`);
        if(targetIp){
            console.log(`Routing ${hostname} to ${targetIp}`);
            return targetIp;
        }
    }
    catch (err) {
        console.error('Error occurred while fetching target IP:', err);
    }

    return null;
}

//HELPER: Ensure target has the correct format

const getTargetUrl = (ip) => {
    return ip.includes(':') ? `http://${ip}` : `http://${ip}:5173`;
};

const server = http.createServer(async (req, res) => {
    const rawHost = req.headers.host || '';
    const hostname = rawHost.split(':')[0]; // Extract hostname without port

    const targetIp = await getTarget(hostname);

    if(!targetIp){
        res.writeHead(404, {'Content-Type': 'text/plain'});
        return res.end(`Preview not found for ${hostname}`);
    }

    const target = getTargetUrl(targetIp);
    console.log(`Proxying request for ${hostname} to ${target} ${req.url}`);

    proxy.web(req, res, { target }, (err) => {
        console.error(`Error proxying request to ${hostname}:`, err);
        if(!res.headersSent){
            res.writeHead(502, {'Content-Type': 'text/plain'});
            res.end(`Bad Gateway: Unable to proxy request to ${hostname}, Vite server unavailable...`);
        }
    });
});

server.on('upgrade',async (req, socket, head) => {
    const rawHost = req.headers.host || '';
    const hostname = rawHost.split(':')[0];

    const targetIp = await getTarget(hostname);

    if(targetIp){
        const target = getTargetUrl(targetIp);
        console.log(`Proxying WebSocket upgrade for ${hostname} to ${target} ${req.url}`);
        proxy.ws(req, socket, head, { target }, (err) => {
            console.error(`Error proxying WebSocket upgrade to ${hostname}:`, err);
            socket.destroy();
        });
    }else{
        socket.destroy();
    }
});

server.listen(80, () => console.log('Proxy server listening on port 80'));