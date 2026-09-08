// ========================================================
// APPLICATION FRONTEND - GESTION DE PROJETS ÉTUDIANTS
// ========================================================

const state = {
    token: localStorage.getItem('token') || null,
    currentUser: null,
    subjects: [],
    myGroup: null
};

// ========================================================
// 1. AUTHENTIFICATION & UTILITAIRES
// ========================================================

async function apiFetch(endpoint, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (state.token) headers['Authorization'] = `Bearer ${state.token}`;

    const res = await fetch(endpoint, { ...options, headers });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || `Erreur (${res.status})`);
    return data;
}

function fillLogin(login, password) {
    document.getElementById('login-input').value = login;
    document.getElementById('password-input').value = password;
}

function logout() {
    state.token = null;
    state.currentUser = null;
    state.myGroup = null;
    localStorage.removeItem('token');
    document.getElementById('auth-section').classList.remove('hidden');
    document.getElementById('dashboard-section').classList.add('hidden');
    document.getElementById('user-nav').classList.add('hidden');
}

async function checkAuth() {
    state.currentUser = await apiFetch('/api/me');
    showDashboard();
}

function showDashboard() {
    document.getElementById('auth-section').classList.add('hidden');
    document.getElementById('dashboard-section').classList.remove('hidden');
    document.getElementById('user-nav').classList.remove('hidden');

    const u = state.currentUser;
    document.getElementById('user-info').textContent = `${u.firstName || ''} ${u.lastName || ''} (${u.role})`;
    setupRoleTabs(u.role);
}

function setupRoleTabs(role) {
    const tabs = document.getElementById('role-tabs');
    tabs.innerHTML = '';
    const items = [];

    if (role === 'ADMIN') {
        items.push({ id: 'view-admin-teachers', label: 'Enseignants', cb: loadAdminTeachers });
        items.push({ id: 'view-admin-students', label: 'Étudiants', cb: loadAdminStudents });
        items.push({ id: 'view-subjects', label: 'Sujets', cb: loadSubjects });
    } else if (role === 'TEACHER') {
        items.push({ id: 'view-subjects', label: 'Tous les Sujets', cb: loadSubjects });
        items.push({ id: 'view-my-subjects', label: 'Mes Sujets', cb: loadMySubjects });
        items.push({ id: 'view-create-subject', label: 'Proposer un Sujet' });
    } else if (role === 'STUDENT') {
        items.push({ id: 'view-subjects', label: 'Sujets', cb: loadSubjects });
        items.push({ id: 'view-group', label: 'Mon Groupe', cb: loadStudentGroup });
        items.push({ id: 'view-preferences', label: 'Préférences', cb: loadPreferencesView });
    }

    items.forEach((item, idx) => {
        const btn = document.createElement('button');
        btn.className = `tab-btn ${idx === 0 ? 'active' : ''}`;
        btn.textContent = item.label;
        btn.onclick = () => {
            document.querySelectorAll('.tab-pane').forEach(el => el.classList.add('hidden'));
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.getElementById(item.id).classList.remove('hidden');
            btn.classList.add('active');
            if (item.cb) item.cb();
        };
        tabs.appendChild(btn);
    });

    if (items.length > 0) {
        document.querySelectorAll('.tab-pane').forEach(el => el.classList.add('hidden'));
        document.getElementById(items[0].id).classList.remove('hidden');
        if (items[0].cb) items[0].cb();
    }
}

// ========================================================
// 2. ENSEIGNANT (Sujets : US6, US7, US8)
// ========================================================

async function loadSubjects() {
    try {
        state.subjects = await apiFetch('/api/subjects');
        document.getElementById('subjects-count-badge').textContent = `${state.subjects.length} sujet(s)`;
        const list = document.getElementById('subjects-list');
        list.innerHTML = state.subjects.length === 0 ? '<p class="text-muted">Aucun sujet.</p>' : '';

        state.subjects.forEach(s => {
            const card = document.createElement('div');
            card.className = 'card';
            card.innerHTML = `
                <h3>${escapeHtml(s.title)}</h3>
                <p class="text-muted">${escapeHtml(s.description)}</p>
                <div style="margin-top:0.75rem; font-size:0.85rem;">
                    Responsable : <strong>${escapeHtml(s.teacherName || 'Enseignant')}</strong> (#${s.id})
                </div>
            `;
            list.appendChild(card);
        });
    } catch (err) {
        alert(err.message);
    }
}

async function loadMySubjects() {
    try {
        const mySubjects = state.subjects.filter(s => s.teacherId === state.currentUser.id);
        const list = document.getElementById('my-subjects-list');
        list.innerHTML = mySubjects.length === 0 ? '<p class="text-muted">Vous n\'avez pas encore créé de sujet.</p>' : '';

        mySubjects.forEach(s => {
            const card = document.createElement('div');
            card.className = 'card';
            card.innerHTML = `
                <h3>${escapeHtml(s.title)}</h3>
                <p class="text-muted">${escapeHtml(s.description)}</p>
                <div style="margin-top:0.75rem; display:flex; justify-content:space-between; align-items:center;">
                    <span class="badge">Sujet #${s.id}</span>
                    <button class="btn-outline btn-sm" onclick="openEditModal(${s.id}, '${escapeQuote(s.title)}', '${escapeQuote(s.description)}')">Modifier</button>
                </div>
            `;
            list.appendChild(card);
        });
    } catch (err) {
        alert(err.message);
    }
}

function openEditModal(id, title, description) {
    document.getElementById('edit-subject-id').value = id;
    document.getElementById('edit-subject-title').value = title;
    document.getElementById('edit-subject-desc').value = description;
    document.getElementById('edit-subject-modal').classList.remove('hidden');
}

function closeEditModal() {
    document.getElementById('edit-subject-modal').classList.add('hidden');
}

// ========================================================
// 3. ÉTUDIANT (Groupes & Préférences : US10, US11, US14)
// ========================================================

async function loadStudentGroup() {
    try {
        console.log("load")
        state.myGroup = await apiFetch('/api/my-group').catch(() => null);
        const noGroup = document.getElementById('no-group-container');
        const hasGroup = document.getElementById('has-group-container');

        if (state.myGroup && state.myGroup.id) {
            noGroup.classList.add('hidden');
            hasGroup.classList.remove('hidden');
            document.getElementById('my-group-name').textContent = state.myGroup.name;
            document.getElementById('group-members-count').textContent = `${state.myGroup.members.length} / 4 membres`;

            const list = document.getElementById('my-group-members-list');
            list.innerHTML = '';
            state.myGroup.members.forEach(m => {
                const li = document.createElement('li');
                li.innerHTML = `<span><strong>${escapeHtml(m.firstName || '')} ${escapeHtml(m.lastName || '')}</strong> (${m.login})</span>`;
                list.appendChild(li);
            });

            // ----------------------------------------------------
            // ici appel à GET /api/groups/{id} pour afficher les préférences
            // ----------------------------------------------------

            const group = await apiFetch(`/api/groups/${state.myGroup.id}`, { method: 'GET'});
            const rankedSubjects = group.rankedSubjects;
            console.log(group);



            const preferencesList = document.getElementById('group-preferences-display');
            if (rankedSubjects.length === 0) {
                preferencesList.innerHTML = '<div class="hint" style="align-items: center">Aucun sujets trouvés.</div>';
                return;
            }
            preferencesList.innerHTML = '';
            preferencesList.innerHTML += '<h3>Préférences des sujets: 1->5</h3>'
            rankedSubjects.forEach(s => {
                const d = document.createElement('div');
                d.innerHTML = `<span><strong>${escapeHtml(s.title || '')}</strong>: <div class="badge">${escapeHtml(s.description || '')}</div></span>
<div class="hint">Encadrant: ${escapeHtml(s.teacherName || '')}</div> `;
                preferencesList.appendChild(d);
            });

        } else {
            noGroup.classList.remove('hidden');
            hasGroup.classList.add('hidden');
            loadAvailableGroups();
        }
    } catch (err) {
        alert(err.message);
    }
}

async function loadAvailableGroups() {
    const container = document.getElementById('available-groups-list');
    try {
        const groups = await apiFetch('/api/groups');
        container.innerHTML = groups.length === 0 ? '<p class="text-muted">Aucun groupe créé.</p>' : '';

        groups.forEach(g => {
            const div = document.createElement('div');
            div.className = 'group-item';
            const isFull = g.members.length >= 4;
            div.innerHTML = `
                <div>
                    <strong>${escapeHtml(g.name)}</strong> (${g.members.length}/4)
                </div>
                ${isFull ? '<span class="badge">Complet</span>' : `<button class="btn-outline btn-sm" onclick="joinGroup(${g.id})">Rejoindre</button>`}
            `;
            container.appendChild(div);
        });
    } catch (err) {
        container.innerHTML = '<p class="text-muted">Erreur de chargement.</p>';
    }
}

async function joinGroup(groupId) {
    try {
        const group = await apiFetch(`/api/groups/${groupId}/join`, { method: 'POST' });
        alert(`Vous avez rejoint "${group.name}" !`);
        loadStudentGroup();
    } catch (err) {
        alert(err.message);
    }
}

async function loadPreferencesView() {
    if (!state.myGroup) await loadStudentGroup();
    if (!state.subjects.length) await loadSubjects();

    ['pref-1', 'pref-2', 'pref-3', 'pref-4', 'pref-5'].forEach((id, idx) => {
        const sel = document.getElementById(id);
        sel.innerHTML = `<option value="">-- Choix n°${idx + 1} --</option>`;
        state.subjects.forEach(s => {
            sel.innerHTML += `<option value="${s.id}">#${s.id} - ${s.title}</option>`;
        });
    });
}

// ========================================================
// 4. ADMINISTRATEUR (Gestion Comptes : US2, US3)
// ========================================================

async function loadAdminTeachers() {
    const teachers = await apiFetch('/api/teachers');
    const list = document.getElementById('admin-teachers-list');
    list.innerHTML = '';
    teachers.forEach(t => {
        list.innerHTML += `<li><span>${escapeHtml(t.firstName || '')} ${escapeHtml(t.lastName || '')} (${t.login})</span><span class="badge">Enseignant</span></li>`;
    });
}

async function loadAdminStudents() {
    const students = await apiFetch('/api/students');
    const list = document.getElementById('admin-students-list');
    list.innerHTML = '';
    students.forEach(s => {
        list.innerHTML += `<li><span>${escapeHtml(s.firstName || '')} ${escapeHtml(s.lastName || '')} (${s.login})</span><span class="badge">Étudiant</span></li>`;
    });
}

// ========================================================
// INITIALISATION & ÉCOUTEURS DE FORMULAIRES
// ========================================================

document.addEventListener('DOMContentLoaded', () => {
    // Connexion
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const login = document.getElementById('login-input').value.trim();
        const password = document.getElementById('password-input').value;
        try {
            const res = await apiFetch('/api/login', { method: 'POST', body: JSON.stringify({ login, password }) });
            state.token = res.token;
            localStorage.setItem('token', res.token);
            state.currentUser = res;
            showDashboard();
        } catch (err) {
            alert(err.message);
        }
    });

    document.getElementById('logout-btn').addEventListener('click', logout);

    // Créer Sujet (Enseignant)
    document.getElementById('create-subject-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const title = document.getElementById('subject-title').value.trim();
        const description = document.getElementById('subject-description').value.trim();
        try {
            await apiFetch('/api/subjects', { method: 'POST', body: JSON.stringify({ title, description }) });
            alert('Sujet publié avec succès !');
            e.target.reset();
            loadSubjects();
        } catch (err) {
            alert(err.message);
        }
    });

    // Modifier Sujet (Enseignant)
    document.getElementById('edit-subject-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('edit-subject-id').value;
        const title = document.getElementById('edit-subject-title').value.trim();
        const description = document.getElementById('edit-subject-desc').value.trim();
        try {
            await apiFetch(`/api/subjects/${id}`, { method: 'PUT', body: JSON.stringify({ title, description }) });
            alert('Sujet mis à jour avec succès !');
            closeEditModal();
            loadSubjects();
            loadMySubjects();
        } catch (err) {
            alert(err.message);
        }
    });

    // Créer Groupe (Étudiant)
    document.getElementById('create-group-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('group-name-input').value.trim();
        try {
            const g = await apiFetch('/api/groups', { method: 'POST', body: JSON.stringify({ name }) });
            alert(`Groupe "${g.name}" créé avec succès !`);
            e.target.reset();
            loadStudentGroup();
        } catch (err) {
            alert(err.message);
        }
    });

    // Saisir Préférences (Étudiant)
    document.getElementById('preferences-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!state.myGroup) return alert('Aucun groupe actif');

        const subjectIds = [1, 2, 3, 4, 5].map(i => parseInt(document.getElementById(`pref-${i}`).value));
        if (new Set(subjectIds).size !== 5) return alert('Les 5 sujets doivent être distincts !');

        try {
            await apiFetch(`/api/groups/${state.myGroup.id}/preferences`, { method: 'POST', body: JSON.stringify({ subjectIds }) });
            alert('Préférences enregistrées avec succès !');
        } catch (err) {
            alert(err.message);
        }
    });

    // Créer Enseignant (Admin)
    document.getElementById('create-teacher-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const login = document.getElementById('teacher-login').value.trim();
        const password = document.getElementById('teacher-password').value;
        const firstName = document.getElementById('teacher-firstname').value.trim();
        const lastName = document.getElementById('teacher-lastname').value.trim();
        try {
            await apiFetch('/api/teachers', { method: 'POST', body: JSON.stringify({ login, password, firstName, lastName }) });
            alert('Enseignant créé avec succès !');
            e.target.reset();
            loadAdminTeachers();
        } catch (err) {
            alert(err.message);
        }
    });

    // Créer Étudiant (Admin)
    document.getElementById('create-student-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const login = document.getElementById('student-login').value.trim();
        const password = document.getElementById('student-password').value;
        const firstName = document.getElementById('student-firstname').value.trim();
        const lastName = document.getElementById('student-lastname').value.trim();
        try {
            await apiFetch('/api/students', { method: 'POST', body: JSON.stringify({ login, password, firstName, lastName }) });
            alert('Étudiant créé avec succès !');
            e.target.reset();
            loadAdminStudents();
        } catch (err) {
            alert(err.message);
        }
    });

    // Auto-connexion si token présent
    if (state.token) {
        checkAuth().catch(() => logout());
    }
});

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function escapeQuote(str) {
    if (!str) return '';
    return str.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}
