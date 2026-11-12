// Home Portal Logic
document.addEventListener('DOMContentLoaded', () => {
    loadNewReleases();
    loadPopularItems();
    initTicker();
});

async function loadNewReleases() {
    const container = document.getElementById('new-releases-container');
    if (!container) return;

    try {
        const res = await API.get('/api/items/featured');
        if (res.success && res.data && res.data.length > 0) {
            container.innerHTML = res.data.map(item => `
                <div class="carousel-item modern-card" onclick="location.href='Product Detail.html?id=${item.id}'">
                    <div class="card-img-wrapper">
                        <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=400'}" alt="${item.title}">
                        <span class="circle-badge">${item.postalCircle ? item.postalCircle.name : 'All India'}</span>
                    </div>
                    <div class="card-content">
                        <span class="item-type-badge">${item.materialType ? item.materialType.replace('_', ' ') : 'STAMP'}</span>
                        <h4>${item.title}</h4>
                        <p class="theme-tag"><i class="fas fa-tag"></i> ${item.theme} (${item.releaseYear})</p>
                        <div class="card-price-row">
                            <span class="price-val">₹${parseFloat(item.price).toFixed(2)}</span>
                            <button class="btn-sm-order" onclick="event.stopPropagation(); quickOrder(${item.id})">
                                <i class="fas fa-cart-plus"></i> Order
                            </button>
                        </div>
                    </div>
                </div>
            `).join('');
        } else {
            container.innerHTML = `<p style="padding: 24px; color: #64748b;">No new releases available currently.</p>`;
        }
    } catch (e) {
        console.error('Failed to load new releases', e);
    }
}

async function loadPopularItems() {
    const container = document.getElementById('popular-items-container');
    if (!container) return;

    try {
        const res = await API.get('/api/items/popular');
        if (res.success && res.data && res.data.length > 0) {
            container.innerHTML = res.data.slice(0, 6).map(item => `
                <div class="grid-item modern-grid-card" onclick="location.href='Product Detail.html?id=${item.id}'">
                    <div class="grid-card-img-wrapper">
                        <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1561731216-c3a4d99437d5?w=400'}" alt="${item.title}">
                    </div>
                    <div class="grid-card-body">
                        <span class="type-pill">${item.materialType ? item.materialType.replace('_', ' ') : 'STAMP'}</span>
                        <h4>${item.title}</h4>
                        <p class="item-circle"><i class="fas fa-map-marker-alt"></i> ${item.postalCircle ? item.postalCircle.name : 'India Post'}</p>
                        <div class="price-action-row">
                            <span class="price-tag">₹${parseFloat(item.price).toFixed(2)}</span>
                            <button class="btn-secondary" style="padding: 6px 14px; font-size: 0.85rem;" onclick="event.stopPropagation(); quickOrder(${item.id})">
                                <i class="fas fa-wallet"></i> PDA Order
                            </button>
                        </div>
                    </div>
                </div>
            `).join('');
        }
    } catch (e) {
        console.error('Failed to load popular items', e);
    }
}

async function quickOrder(itemId) {
    if (!Auth.isLoggedIn()) {
        showToast('Please sign in to place orders using your Philatelic Deposit Account', 'warning');
        setTimeout(() => location.href = 'login.html', 800);
        return;
    }

    if (!confirm('Place instant order using your National Philately Deposit Account balance?')) {
        return;
    }

    const payload = {
        paymentMethod: 'PDA_WALLET',
        items: [{ itemId: itemId, quantity: 1 }]
    };

    const res = await API.post('/api/orders', payload);
    if (res.success && res.data) {
        showToast(`Order Placed! Consignment Tracking No: ${res.data.trackingNumber}`, 'success');
        Auth.refreshProfile();
    } else {
        showToast(res.message || 'Order failed. Please check PDA balance.', 'error');
    }
}

function initTicker() {
    const tickerTextEl = document.getElementById("ticker-text");
    if (!tickerTextEl) return;

    const updates = [
        "Chandrayaan-3 First Anniversary Commemorative Stamp issued with Special Postmark across Karnataka Circle.",
        "National Philately Deposit Account standing instructions now auto-dispatch Special Covers and Miniature Sheets.",
        "Delhi Postal Circle announces rare philatelic materials exhibition at National Philatelic Museum, Dak Bhawan.",
        "Project Tiger Golden Jubilee pictorial cancellation available this month at Mumbai General Post Office."
    ];
    let updateIndex = 0;
    setInterval(() => {
        updateIndex = (updateIndex + 1) % updates.length;
        tickerTextEl.textContent = updates[updateIndex];
    }, 6000);
}

