// Philatelic Materials Catalog Logic
let currentPage = 0;
const pageSize = 9;
let totalPages = 1;

document.addEventListener('DOMContentLoaded', () => {
    loadPostalCircles();
    loadCatalogItems();

    const circleSelect = document.getElementById('postal-circle');
    const materialSelect = document.getElementById('material-type');
    const yearInput = document.getElementById('year-release');
    const themeSelect = document.getElementById('theme');
    const sortSelect = document.getElementById('sort-options');
    const searchInput = document.getElementById('search-materials');

    if (circleSelect) circleSelect.addEventListener('change', () => { currentPage = 0; loadCatalogItems(); });
    if (materialSelect) materialSelect.addEventListener('change', () => { currentPage = 0; loadCatalogItems(); });
    if (yearInput) yearInput.addEventListener('input', debounce(() => { currentPage = 0; loadCatalogItems(); }, 400));
    if (themeSelect) themeSelect.addEventListener('change', () => { currentPage = 0; loadCatalogItems(); });
    if (sortSelect) sortSelect.addEventListener('change', () => { currentPage = 0; loadCatalogItems(); });
    if (searchInput) searchInput.addEventListener('input', debounce(() => { currentPage = 0; loadCatalogItems(); }, 300));
});

function debounce(func, delay) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), delay);
    };
}

async function loadPostalCircles() {
    const select = document.getElementById('postal-circle');
    if (!select) return;

    try {
        const res = await API.get('/api/circles');
        if (res.success && res.data) {
            const currentVal = select.value;
            select.innerHTML = '<option value="all">All Postal Circles</option>' +
                res.data.map(c => `<option value="${c.id}">${c.name} (${c.code})</option>`).join('');
            if (currentVal && currentVal !== 'all') {
                select.value = currentVal;
            }
        }
    } catch (e) {
        console.error('Failed to load circles', e);
    }
}

async function loadCatalogItems() {
    const grid = document.getElementById('catalog-items-grid');
    if (!grid) return;

    grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 50px 20px; color: #64748b;">
            <i class="fas fa-spinner fa-spin fa-2x" style="color: var(--primary); margin-bottom: 10px;"></i>
            <p>Loading philatelic materials from national repository...</p>
        </div>
    `;

    const circleVal = document.getElementById('postal-circle')?.value;
    const materialVal = document.getElementById('material-type')?.value;
    const yearVal = document.getElementById('year-release')?.value;
    const themeVal = document.getElementById('theme')?.value;
    const sortVal = document.getElementById('sort-options')?.value || 'most-recent';
    const queryVal = document.getElementById('search-materials')?.value || '';

    const params = new URLSearchParams({
        page: currentPage,
        size: pageSize,
        sortBy: sortVal
    });

    if (circleVal && circleVal !== 'all') params.append('circleId', circleVal);
    if (materialVal && materialVal !== 'all') params.append('materialType', materialVal);
    if (yearVal && yearVal.trim() !== '') params.append('releaseYear', yearVal.trim());
    if (themeVal && themeVal !== 'all') params.append('theme', themeVal);
    if (queryVal && queryVal.trim() !== '') params.append('query', queryVal.trim());

    try {
        const res = await API.get(`/api/items?${params.toString()}`);
        if (res.success && res.data) {
            const items = res.data.content || [];
            totalPages = res.data.totalPages || 1;

            if (items.length === 0) {
                grid.innerHTML = `
                    <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; background: white; border-radius: var(--radius-lg); border: 1px solid var(--border-color); box-shadow: var(--shadow-sm);">
                        <i class="fas fa-search fa-3x" style="color: #cbd5e1; margin-bottom: 14px;"></i>
                        <h3 style="color: var(--text-main); margin-bottom: 6px;">No Philatelic Items Found</h3>
                        <p style="color: var(--text-muted); margin-bottom: 20px;">Try adjusting your search query, selecting different postal circles, or resetting filters.</p>
                        <button class="btn-outline" onclick="resetFilters()"><i class="fas fa-redo"></i> Reset All Filters</button>
                    </div>
                `;
            } else {
                grid.innerHTML = items.map(item => `
                    <div class="item">
                        <div class="stamp-thumb-wrapper">
                            <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=400'}" alt="${item.title}">
                            <button class="wishlist-btn" onclick="toggleWishlist(${item.id}, this)" title="Save to Wishlist">
                                <i class="far fa-heart"></i>
                            </button>
                            <span class="stock-status-pill ${item.stockQuantity > 0 ? 'in-stock' : 'out-of-stock'}">
                                ${item.stockQuantity > 0 ? `In Stock (${item.stockQuantity})` : 'Out of Stock'}
                            </span>
                        </div>
                        <div class="item-details">
                            <span class="item-type-badge">${item.materialType ? item.materialType.replace('_', ' ') : 'STAMP'}</span>
                            <h4>${item.title}</h4>
                            <p class="circle-txt"><i class="fas fa-map-marker-alt"></i> ${item.postalCircle ? item.postalCircle.name : 'All India'}</p>
                            <p class="theme-txt"><i class="fas fa-layer-group"></i> ${item.theme} (${item.releaseYear})</p>
                            <div class="price-row">
                                <span class="price-val">₹${parseFloat(item.price).toFixed(2)}</span>
                                <span class="denom">${item.denomination || ''}</span>
                            </div>
                            <div class="actions-row">
                                <button class="add-to-account" onclick="orderFromCatalog(${item.id}, '${escapeHtml(item.title)}', ${item.price})">
                                    <i class="fas fa-wallet"></i> PDA Order
                                </button>
                                <a href="Product Detail.html?id=${item.id}" class="view-details">
                                    Details <i class="fas fa-arrow-right"></i>
                                </a>
                            </div>
                        </div>
                    </div>
                `).join('');
            }

            renderPagination();
        }
    } catch (e) {
        console.error('Error fetching catalog', e);
        grid.innerHTML = `<div style="grid-column: 1 / -1; color: red; text-align: center; padding: 40px;">Failed to load catalog items.</div>`;
    }
}

function renderPagination() {
    const prevBtn = document.querySelector('.pagination .prev');
    const nextBtn = document.querySelector('.pagination .next');
    const pageInd = document.getElementById('page-indicator');

    if (prevBtn) {
        prevBtn.disabled = (currentPage === 0);
        prevBtn.onclick = () => {
            if (currentPage > 0) {
                currentPage--;
                loadCatalogItems();
                window.scrollTo({ top: 120, behavior: 'smooth' });
            }
        };
    }

    if (nextBtn) {
        nextBtn.disabled = (currentPage >= totalPages - 1);
        nextBtn.onclick = () => {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadCatalogItems();
                window.scrollTo({ top: 120, behavior: 'smooth' });
            }
        };
    }

    if (pageInd) {
        pageInd.textContent = `Page ${currentPage + 1} of ${totalPages || 1}`;
    }
}

async function orderFromCatalog(itemId, title, price) {
    if (!Auth.isLoggedIn()) {
        showToast('Please sign in with your National Philately Account to place orders.', 'warning');
        setTimeout(() => location.href = 'login.html', 800);
        return;
    }

    const confirmed = confirm(`Confirm order for "${title}" at ₹${parseFloat(price).toFixed(2)} using your Philatelic Deposit Account balance?`);
    if (!confirmed) return;

    const res = await API.post('/api/orders', {
        paymentMethod: 'PDA_WALLET',
        items: [{ itemId, quantity: 1 }]
    });

    if (res.success && res.data) {
        showToast(`Order Placed! Consignment Tracking No: ${res.data.trackingNumber}`, 'success');
        Auth.refreshProfile();
        loadCatalogItems();
    } else {
        showToast(res.message || 'Order failed. Please check your PDA balance.', 'error');
    }
}

async function toggleWishlist(itemId, btnEl) {
    if (!Auth.isLoggedIn()) {
        showToast('Please sign in to save items to your wishlist.', 'warning');
        return;
    }

    const icon = btnEl.querySelector('i');
    if (icon.classList.contains('fas')) {
        const res = await API.delete(`/api/wishlist/${itemId}`);
        if (res.success) {
            icon.classList.replace('fas', 'far');
            icon.style.color = '';
            showToast('Item removed from wishlist', 'info');
        }
    } else {
        const res = await API.post(`/api/wishlist/${itemId}`);
        if (res.success) {
            icon.classList.replace('far', 'fas');
            icon.style.color = '#ef4444';
            showToast('Item saved to wishlist!', 'success');
        }
    }
}

function resetFilters() {
    if (document.getElementById('postal-circle')) document.getElementById('postal-circle').value = 'all';
    if (document.getElementById('material-type')) document.getElementById('material-type').value = 'all';
    if (document.getElementById('year-release')) document.getElementById('year-release').value = '';
    if (document.getElementById('theme')) document.getElementById('theme').value = 'all';
    if (document.getElementById('sort-options')) document.getElementById('sort-options').value = 'most-recent';
    if (document.getElementById('search-materials')) document.getElementById('search-materials').value = '';
    currentPage = 0;
    loadCatalogItems();
}

function escapeHtml(str) {
    return (str || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
}
