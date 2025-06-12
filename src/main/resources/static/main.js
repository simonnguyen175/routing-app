document.addEventListener('DOMContentLoaded', () => {

    /* 1. Khởi tạo bản đồ Leaflet */
    const map = L.map('map').fitBounds([[18.2593, 104.9702], [19.3772, 107.0508]]);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19, attribution: '&copy; OpenStreetMap'
    }).addTo(map);

    /* 2. LayerGroup để gom marker / polyline */
    const graphLayer = L.layerGroup().addTo(map);

    /* 3. Hàm gọi API & vẽ */
    async function showGraph() {

        graphLayer.clearLayers();               // xoá vẽ cũ

        const b = map.getBounds();
        const modeRadio = document.querySelector('input[name="routeMode"]:checked');
        const mode = modeRadio ? modeRadio.value : 'driving';

        const url = `http://localhost:8080/api/graph` + `?latMin=${b.getSouth()}&latMax=${b.getNorth()}` + `&lonMin=${b.getWest()}&lonMax=${b.getEast()}` + `&mode=${mode}`;

        try {
            const res = await fetch(url);
            if (!res.ok) throw new Error(res.status + ' ' + res.statusText);
            const data = await res.json();

            console.log('[DEBUG]', data.nodes.length, 'nodes,', data.edges.length, 'edges');

            /* --- vẽ Edge trước (để node nằm trên) --- */
            data.edges.forEach(e => {
                if ([e.lat1, e.lon1, e.lat2, e.lon2].some(v => v == null)) return;
                L.polyline([[+e.lat1, +e.lon1], [+e.lat2, +e.lon2]], {
                    color: '#ff0033',
                    weight: 2,
                    opacity: 0.6
                }).addTo(graphLayer);
            });

            /* --- vẽ Node --- */
            data.nodes.forEach(n => {
                if (n.lat == null || n.lon == null) return;
                L.circleMarker([+n.lat, +n.lon], {
                    radius: 5, color: '#0066ff', weight: 1, fillOpacity: 0.9
                }).bindTooltip(`ID: ${n.id}`).addTo(graphLayer);
            });

            /* --- zoom khung dữ liệu --- */
            if (data.nodes.length) {
                const latLngs = data.nodes.map(n => [+n.lat, +n.lon]);
                map.fitBounds(latLngs, {padding: [25, 25], maxZoom: 17});
            }

        } catch (err) {
            console.error('Lỗi khi fetch graph:', err);
        }
    }

    document.getElementById('btnShowGraph').addEventListener('click', showGraph);
    document.getElementById('btnHideGraph')
        .addEventListener('click', () => graphLayer.clearLayers());

    // Trạng thái
    let pickMode = null; // 'from' | 'to'
    let fromMarker = null, toMarker = null;

    // Lấy DOM
    const btnPickFrom = document.getElementById('btnPickFrom');
    const btnPickTo = document.getElementById('btnPickTo');
    const inpFrom = document.getElementById('from-location');
    const inpTo = document.getElementById('to-location');

    // Khi bấm nút chọn FROM
    btnPickFrom.addEventListener('click', () => {
        pickMode = 'from';
        map.getContainer().style.cursor = 'crosshair';
    });
    // Khi bấm nút chọn TO
    btnPickTo.addEventListener('click', () => {
        pickMode = 'to';
        map.getContainer().style.cursor = 'crosshair';
    });

    // Khi click map
    map.on('click', (e) => {
        if (!pickMode) return;
        const {lat, lng} = e.latlng;
        if (pickMode === 'from') {
            if (fromMarker) map.removeLayer(fromMarker);
            fromMarker = L.circleMarker([lat, lng], {
                radius: 5, color: 'black',      // viền đỏ
                fillColor: 'white',  // nền đỏ nhạt
                fillOpacity: 0.8
            }).addTo(map);
            inpFrom.value = `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
        } else if (pickMode === 'to') {
            if (toMarker) map.removeLayer(toMarker);
            toMarker = L.marker([lat, lng]).addTo(map);
            inpTo.value = `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
        }
        pickMode = null;
        map.getContainer().style.cursor = '';
    });


    const btnFindRoute = document.getElementById('btnFindRoute');
    let routeLayer = null;

    btnFindRoute.addEventListener('click', async () => {
        if (!inpFrom.value || !inpTo.value) {
            alert('Vui lòng chọn đủ 2 điểm!');
            return;
        }
        const [fromLat, fromLon] = inpFrom.value.split(',').map(Number);
        const [toLat, toLon] = inpTo.value.split(',').map(Number);
        const routeType = document.querySelector('input[name="routeMode"]:checked').value;

        const res = await fetch(`/api/route?fromLat=${fromLat}&fromLon=${fromLon}&toLat=${toLat}&toLon=${toLon}&routeType=${routeType}`);
        if (!res.ok) {
            alert('Không tìm được đường phù hợp!');
            return;
        }
        const data = await res.json(); // { nodes:[], edges:[] }

        // build polyline from the node array
        const coords = data.map(node => [node.lat, node.lon]);
        if (routeLayer) map.removeLayer(routeLayer);
        routeLayer = L.featureGroup().addTo(map);

        // 2. Vẽ dotline từ điểm chọn đến node đầu
        const startNode = data[0];
        L.polyline(
            [[fromLat, fromLon], [startNode.lat, startNode.lon]],
            {color: 'blue', weight: 3, dashArray: '2, 10', opacity: 0.9}
        ).addTo(routeLayer);

        // 3. Vẽ dotline từ điểm chọn đến node cuối
        const endNode = data[data.length - 1];
        L.polyline(
            [[toLat, toLon], [endNode.lat, endNode.lon]],
            {color: 'blue', weight: 3, dashArray: '2, 10', opacity: 0.9}
        ).addTo(routeLayer);

        function approxDistance(lat1, lon1, lat2, lon2) {
            const R = 111320; // mét trên 1 độ
            const avgLat = (lat1 + lat2) / 2 * Math.PI / 180;
            const dLat = (lat2 - lat1) * R;
            const dLon = (lon2 - lon1) * R * Math.cos(avgLat);
            return Math.sqrt(dLat * dLat + dLon * dLon);
        }

        let totalDist = 0;
        totalDist += approxDistance(fromLat, fromLon, data[0].lat, data[0].lon);
        for (let i = 1; i < data.length; i++) {
            const from = data[i - 1];
            const to = data[i];
            L.polyline([[from.lat, from.lon], [to.lat, to.lon]], {
                color: 'blue',
                weight: 3,
                dashArray: routeType === 'walking' ? '2, 10' : null
            }).addTo(routeLayer);
            totalDist += approxDistance(data[i-1].lat, data[i-1].lon, data[i].lat, data[i].lon);
        }
        totalDist += approxDistance(toLat, toLon, data[data.length - 1].lat, data[data.length - 1].lon);

        // Hiển thị info
        const infoDiv = document.getElementById('route-info');
        let distanceText;
        if (totalDist >= 1000) {
            distanceText = `${(totalDist / 1000).toFixed(2)} km`;
        } else {
            distanceText = `${Math.round(totalDist)} m`;
        }
        infoDiv.innerText = `Fastest route: ${distanceText}`;

        map.fitBounds(routeLayer.getBounds());
    });

    btnHideRoute.addEventListener('click', () => {
        if (routeLayer) {
            map.removeLayer(routeLayer);
            routeLayer = null;
        }
        if (toMarker) {
            map.removeLayer(toMarker);
            toMarker = null;
        }
        if (fromMarker) {
            map.removeLayer(fromMarker);
            fromMarker = null;
        }
        inpFrom.value = '';
        inpTo.value = '';
        document.getElementById('route-info').innerText = '';
    });
});
