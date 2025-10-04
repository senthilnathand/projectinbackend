const state = {
	baseUrl: '',
	token: localStorage.getItem('token') || null,
	username: localStorage.getItem('username') || null,
	role: localStorage.getItem('role') || null,
};

const el = (id) => document.getElementById(id);

async function api(path, options = {}) {
	const headers = options.headers || {};
	if (state.token) headers['Authorization'] = `Bearer ${state.token}`;
	if (options.body && !headers['Content-Type']) headers['Content-Type'] = 'application/json';
	const res = await fetch(`${state.baseUrl}${path}`, { ...options, headers });
	if (!res.ok) throw new Error(await res.text());
	return res.json();
}

function renderApp() {
	const authSection = el('authSection');
	const appSection = el('appSection');
	const userBar = el('userBar');
    const managerPanel = document.getElementById('managerPanel');
	if (state.token) {
		authSection.classList.add('hidden');
		appSection.classList.remove('hidden');
		userBar.classList.remove('hidden');
		el('whoami').textContent = `${state.username} (${state.role})`;
		managerPanel.style.display = state.role === 'MANAGER' ? 'block' : 'none';
		refreshLists();
	} else {
		authSection.classList.remove('hidden');
		appSection.classList.add('hidden');
		userBar.classList.add('hidden');
	}
}

async function refreshLists() {
	// my tasks
	const myTasks = await api('/api/tasks/mine');
	renderTasks('myTasks', myTasks, true);
    // assigned by me (if manager)
	if (state.role === 'MANAGER') {
		const byMe = await api('/api/tasks/assigned-by-me');
		renderTasks('byMeTasks', byMe, false);
	}
}

function renderTasks(containerId, tasks, canUpdate) {
	const c = el(containerId);
	c.innerHTML = '';
	if (!tasks.length) {
		c.innerHTML = '<p class="muted">No tasks yet.</p>';
		return;
	}
	tasks.forEach(t => {
		const div = document.createElement('div');
		div.className = 'card';
        const due = t.dueDate ? new Date(t.dueDate).toLocaleDateString() : 'No due date';
        div.innerHTML = `
			<div class="row">
				<div>
					<div class="font-medium text-slate-800">${escapeHtml(t.title)}</div>
                    <div class="muted text-sm">${escapeHtml(t.description || '')}</div>
                    <div class="text-sm mt-1"><span class="muted">Due:</span> ${due}</div>
				</div>
				<div class="tag">${t.status}</div>
			</div>
		`;
		if (canUpdate) {
			const sel = document.createElement('select');
			sel.className = 'input mt-2';
			['PENDING','IN_PROGRESS','COMPLETED'].forEach(s => {
				const opt = document.createElement('option');
				opt.value = s; opt.textContent = s; if (t.status === s) opt.selected = true; sel.appendChild(opt);
			});
			sel.addEventListener('change', async () => {
				await api(`/api/tasks/${t.id}/status`, { method: 'PATCH', body: JSON.stringify({ status: sel.value }) });
				refreshLists();
			});
			div.appendChild(sel);
		}
		c.appendChild(div);
	});
}

function escapeHtml(str) {
	return str.replace(/[&<>"] /g, (c) => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',' ':' '}[c]));
}

// events
document.addEventListener('DOMContentLoaded', () => {
	// login
	el('loginForm').addEventListener('submit', async (e) => {
		e.preventDefault();
		const username = el('username').value.trim();
		const password = el('password').value;
		try {
			const data = await api('/api/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) });
			state.token = data.token;
			state.username = username;
			localStorage.setItem('token', state.token);
			localStorage.setItem('username', username);
            // fetch role via /api/auth/me
            try { const me = await api('/api/auth/me'); state.role = (me.roles||[]).includes('MANAGER') ? 'MANAGER' : 'EMPLOYEE'; }
            catch { state.role = 'EMPLOYEE'; }
			localStorage.setItem('role', state.role);
			el('authMsg').classList.add('hidden');
			renderApp();
		} catch (err) {
			el('authMsg').textContent = 'Invalid credentials';
			el('authMsg').classList.remove('hidden');
		}
	});

    function doLogout() {
        state.token = null;
        state.username = null;
        state.role = null;
        try { localStorage.removeItem('token'); } catch {}
        try { localStorage.removeItem('username'); } catch {}
        try { localStorage.removeItem('role'); } catch {}
        window.location.href = '/login.html';
    }
    window.appLogout = doLogout;
    const logoutBtn = el('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => { e.preventDefault(); doLogout(); });
    }

	// assign form
    const assignForm = document.getElementById('assignForm');
	assignForm.addEventListener('submit', async (e) => {
		e.preventDefault();
		const title = el('tTitle').value.trim();
		const description = el('tDesc').value.trim();
		const assignedTo = el('tAssignee').value.trim();
        const dueDate = el('tDue') ? el('tDue').value : '';
        await api('/api/tasks', { method: 'POST', body: JSON.stringify({ title, description, assignedTo, dueDate }) });
		assignForm.reset();
		refreshLists();
	});

	renderApp();
});


