// API Base URL - works directly via file://, Live Server, or Spring Boot static hosting
const API_BASE = (window.location.protocol === 'http:' || window.location.protocol === 'https:') 
    ? (window.location.port === '8080' ? '' : 'http://localhost:8080') 
    : 'http://localhost:8080';

// App State
let state = {
    students: [],
    teachers: [],
    courses: [],
    classrooms: [],
    timetables: [],
    currentPage: 'dashboard'
};

const WEEK_DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
const TIME_SLOTS = [
    { start: '09:00', end: '10:00', label: '09:00 - 10:00' },
    { start: '10:00', end: '11:00', label: '10:00 - 11:00' },
    { start: '11:00', end: '12:00', label: '11:00 - 12:00' },
    { start: '12:00', end: '13:00', label: '12:00 - 13:00' },
    { start: '14:00', end: '15:00', label: '14:00 - 15:00' },
    { start: '15:00', end: '16:00', label: '15:00 - 16:00' },
    { start: '16:00', end: '17:00', label: '16:00 - 17:00' }
];

// Initialize application on DOM load
document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();
    loadAllData();
});

// Navigation Handling
function setupNavigation() {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const target = link.getAttribute('data-page');
            navigateTo(target);
        });
    });
}

function navigateTo(pageId) {
    state.currentPage = pageId;

    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    const activeLink = document.querySelector(`.nav-link[data-page="${pageId}"]`);
    if (activeLink) activeLink.classList.add('active');

    document.querySelectorAll('.page-section').forEach(s => s.classList.remove('active'));
    const targetSection = document.getElementById(`section-${pageId}`);
    if (targetSection) targetSection.classList.add('active');

    const titles = {
        dashboard: { title: 'Dashboard', sub: 'Overview and college timetable summary' },
        weekly: { title: 'Weekly Timetable', sub: 'Matrix timetable view for all weekdays' },
        timetables: { title: 'Timetable Management', sub: 'Manage and customize individual class schedules' },
        generate: { title: 'AI Timetable Generator', sub: 'Automated conflict-free schedule synthesizer' },
        students: { title: 'Students Management', sub: 'Learner directories and departments' },
        teachers: { title: 'Teachers Management', sub: 'Faculty staff and course specialization' },
        courses: { title: 'Courses Management', sub: 'Academic curriculum and course credits' },
        classrooms: { title: 'Classrooms Management', sub: 'Campus buildings, room capacities and halls' }
    };

    if (titles[pageId]) {
        document.getElementById('topbar-title').innerText = titles[pageId].title;
        document.getElementById('topbar-subtitle').innerText = titles[pageId].sub;
    }

    if (pageId === 'weekly') {
        renderWeeklyTable();
    }
}

// Data Fetching
async function loadAllData() {
    try {
        await Promise.all([
            fetchStudents(),
            fetchTeachers(),
            fetchCourses(),
            fetchClassrooms(),
            fetchTimetables()
        ]);
        updateDashboardStats();
        renderWeeklyTable();
        renderDashboardPreview();
        populateDropdowns();
    } catch (err) {
        console.error('Error loading initial data:', err);
    }
}

async function fetchStudents() {
    try {
        const res = await fetch(`${API_BASE}/api/students`);
        if (!res.ok) throw new Error('Failed to load students');
        state.students = await res.json();
        renderStudentsTable();
    } catch (e) {
        showToast('Error', e.message, 'error');
    }
}

async function fetchTeachers() {
    try {
        const res = await fetch(`${API_BASE}/api/teachers`);
        if (!res.ok) throw new Error('Failed to load teachers');
        state.teachers = await res.json();
        renderTeachersTable();
    } catch (e) {
        showToast('Error', e.message, 'error');
    }
}

async function fetchCourses() {
    try {
        const res = await fetch(`${API_BASE}/api/courses`);
        if (!res.ok) throw new Error('Failed to load courses');
        state.courses = await res.json();
        renderCoursesTable();
    } catch (e) {
        showToast('Error', e.message, 'error');
    }
}

async function fetchClassrooms() {
    try {
        const res = await fetch(`${API_BASE}/api/classrooms`);
        if (!res.ok) throw new Error('Failed to load classrooms');
        state.classrooms = await res.json();
        renderClassroomsTable();
    } catch (e) {
        showToast('Error', e.message, 'error');
    }
}

async function fetchTimetables() {
    try {
        const res = await fetch(`${API_BASE}/api/timetables`);
        if (!res.ok) throw new Error('Failed to load timetables');
        state.timetables = await res.json();
        renderTimetablesTable();
    } catch (e) {
        showToast('Error', e.message, 'error');
    }
}

// Dashboard Update
function updateDashboardStats() {
    document.getElementById('stat-students').innerText = state.students.length;
    document.getElementById('stat-teachers').innerText = state.teachers.length;
    document.getElementById('stat-courses').innerText = state.courses.length;
    document.getElementById('stat-classrooms').innerText = state.classrooms.length;
    document.getElementById('stat-timetables').innerText = state.timetables.length;

    const autoCount = state.timetables.filter(t => t.autoGenerated).length;
    const manualCount = state.timetables.length - autoCount;
    document.getElementById('stat-timetable-breakdown').innerText = `${manualCount} Manual / ${autoCount} Auto`;
}

// Populate Modal Select Dropdowns
function populateDropdowns() {
    const teacherSelect = document.getElementById('tt-teacher');
    const courseSelect = document.getElementById('tt-course');
    const classroomSelect = document.getElementById('tt-classroom');

    const filterTeacher = document.getElementById('weekly-filter-teacher');
    const filterClassroom = document.getElementById('weekly-filter-classroom');

    if (teacherSelect) {
        teacherSelect.innerHTML = '<option value="">Select Teacher</option>' +
            state.teachers.map(t => `<option value="${t.id}">${t.name} (${t.subject} - ${t.department})</option>`).join('');
    }

    if (courseSelect) {
        courseSelect.innerHTML = '<option value="">Select Course</option>' +
            state.courses.map(c => `<option value="${c.id}">${c.courseCode} - ${c.courseName} (${c.department})</option>`).join('');
    }

    if (classroomSelect) {
        classroomSelect.innerHTML = '<option value="">Select Classroom</option>' +
            state.classrooms.map(c => `<option value="${c.id}">${c.building} - ${c.roomNumber} (Cap: ${c.capacity})</option>`).join('');
    }

    if (filterTeacher) {
        const currentVal = filterTeacher.value;
        filterTeacher.innerHTML = '<option value="">All Teachers</option>' +
            state.teachers.map(t => `<option value="${t.id}">${t.name}</option>`).join('');
        filterTeacher.value = currentVal;
    }

    if (filterClassroom) {
        const currentVal = filterClassroom.value;
        filterClassroom.innerHTML = '<option value="">All Classrooms</option>' +
            state.classrooms.map(c => `<option value="${c.id}">${c.roomNumber} (${c.building})</option>`).join('');
        filterClassroom.value = currentVal;
    }
}

// -------------------------------------------------------------
// WEEKLY TIMETABLE RENDERING
// -------------------------------------------------------------
function renderWeeklyTable() {
    const container = document.getElementById('weekly-grid');
    if (!container) return;

    const filterTeacherId = document.getElementById('weekly-filter-teacher')?.value;
    const filterClassroomId = document.getElementById('weekly-filter-classroom')?.value;

    let filteredTimetables = state.timetables;
    if (filterTeacherId) {
        filteredTimetables = filteredTimetables.filter(t => t.teacher && t.teacher.id == filterTeacherId);
    }
    if (filterClassroomId) {
        filteredTimetables = filteredTimetables.filter(t => t.classroom && t.classroom.id == filterClassroomId);
    }

    let html = `<table class="weekly-table">
        <thead>
            <tr>
                <th class="time-col-header">Time Slot</th>
                ${WEEK_DAYS.map(day => `<th>${day}</th>`).join('')}
            </tr>
        </thead>
        <tbody>`;

    TIME_SLOTS.forEach(slot => {
        html += `<tr>
            <td class="weekly-time-slot">${slot.label}</td>`;

        WEEK_DAYS.forEach(day => {
            // Find class matching day and overlapping slot
            const entries = filteredTimetables.filter(t => {
                if (!t.day || t.day.toLowerCase() !== day.toLowerCase()) return false;
                const startStr = formatTime(t.startTime);
                const endStr = formatTime(t.endTime);
                return startStr < slot.end && endStr > slot.start;
            });

            html += `<td class="weekly-cell">`;
            if (entries.length > 0) {
                entries.forEach(entry => {
                    const isAuto = entry.autoGenerated;
                    html += `
                        <div class="weekly-card ${isAuto ? 'auto' : ''}" onclick="viewTimetableDetails(${entry.id})">
                            <div class="weekly-card-title">${entry.course ? entry.course.courseName : 'Class'}</div>
                            <div class="weekly-card-sub">${entry.teacher ? entry.teacher.name : 'Staff'}</div>
                            <div class="weekly-card-footer">
                                <span class="weekly-card-room">${entry.classroom ? entry.classroom.roomNumber : ''}</span>
                                <span class="badge ${isAuto ? 'badge-auto' : 'badge-manual'}">${isAuto ? 'Auto' : 'Manual'}</span>
                            </div>
                        </div>
                    `;
                });
            } else {
                html += `<div class="empty-slot">-</div>`;
            }
            html += `</td>`;
        });

        html += `</tr>`;
    });

    html += `</tbody></table>`;
    container.innerHTML = html;
}

function renderDashboardPreview() {
    const container = document.getElementById('dashboard-weekly-preview');
    if (!container) return;

    let html = `<table class="weekly-table" style="min-width: 600px;">
        <thead>
            <tr>
                <th class="time-col-header" style="width: 100px;">Time</th>
                ${WEEK_DAYS.map(day => `<th>${day}</th>`).join('')}
            </tr>
        </thead>
        <tbody>`;

    TIME_SLOTS.slice(0, 3).forEach(slot => {
        html += `<tr>
            <td class="weekly-time-slot">${slot.label}</td>`;

        WEEK_DAYS.forEach(day => {
            const entry = state.timetables.find(t => {
                if (!t.day || t.day.toLowerCase() !== day.toLowerCase()) return false;
                const startStr = formatTime(t.startTime);
                const endStr = formatTime(t.endTime);
                return startStr < slot.end && endStr > slot.start;
            });

            html += `<td class="weekly-cell" style="min-height: 60px; height: 60px;">`;
            if (entry) {
                html += `
                    <div class="weekly-card ${entry.autoGenerated ? 'auto' : ''}" style="padding: 4px 6px;">
                        <div class="weekly-card-title" style="font-size: 0.75rem;">${entry.course ? entry.course.courseName : 'Class'}</div>
                        <div class="weekly-card-sub" style="font-size: 0.7rem;">${entry.classroom ? entry.classroom.roomNumber : ''} (${entry.teacher ? entry.teacher.name : ''})</div>
                    </div>
                `;
            } else {
                html += `<div class="empty-slot">-</div>`;
            }
            html += `</td>`;
        });

        html += `</tr>`;
    });

    html += `</tbody></table>`;
    container.innerHTML = html;
}

// -------------------------------------------------------------
// TIMETABLE CRUD & MANAGEMENT
// -------------------------------------------------------------
function renderTimetablesTable(data = state.timetables) {
    const tbody = document.getElementById('timetable-tbody');
    if (!tbody) return;

    if (data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="empty-state">No timetable records found. Click 'Add Timetable' or 'Generate AI' to schedule classes.</td></tr>`;
        return;
    }

    tbody.innerHTML = data.map(t => `
        <tr>
            <td><strong>#${t.id}</strong></td>
            <td><strong>${t.day}</strong></td>
            <td>${formatTime(t.startTime)}</td>
            <td>${formatTime(t.endTime)}</td>
            <td>${t.teacher ? t.teacher.name : '<span class="text-muted">Unassigned</span>'}</td>
            <td>${t.course ? `${t.course.courseCode} - ${t.course.courseName}` : '<span class="text-muted">Unassigned</span>'}</td>
            <td>${t.classroom ? `${t.classroom.roomNumber} (${t.classroom.building})` : '<span class="text-muted">Unassigned</span>'}</td>
            <td>
                <span class="badge ${t.autoGenerated ? 'badge-auto' : 'badge-manual'}">
                    ${t.autoGenerated ? 'Auto Generated' : 'Manual'}
                </span>
            </td>
            <td>
                <div style="display: flex; gap: 6px;">
                    <button class="btn btn-secondary btn-sm" onclick="editTimetable(${t.id})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteTimetable(${t.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function filterTimetableTable() {
    const search = document.getElementById('timetable-search').value.toLowerCase();
    const day = document.getElementById('timetable-filter-day').value;
    const type = document.getElementById('timetable-filter-type').value;

    let filtered = state.timetables.filter(t => {
        const matchesDay = !day || t.day.toLowerCase() === day.toLowerCase();
        const matchesType = !type || (type === 'auto' ? t.autoGenerated : !t.autoGenerated);
        const matchesSearch = !search ||
            (t.day && t.day.toLowerCase().includes(search)) ||
            (t.teacher && t.teacher.name.toLowerCase().includes(search)) ||
            (t.course && (t.course.courseName.toLowerCase().includes(search) || t.course.courseCode.toLowerCase().includes(search))) ||
            (t.classroom && t.classroom.roomNumber.toLowerCase().includes(search));

        return matchesDay && matchesType && matchesSearch;
    });

    renderTimetablesTable(filtered);
}

function openCreateTimetableModal() {
    document.getElementById('timetable-modal-title').innerText = 'Add Timetable';
    document.getElementById('timetable-id').value = '';
    document.getElementById('form-timetable').reset();
    document.getElementById('tt-start-time').value = '09:00';
    document.getElementById('tt-end-time').value = '10:00';
    document.getElementById('tt-auto').checked = false;
    openModal('modal-timetable');
}

async function editTimetable(id) {
    try {
        const res = await fetch(`${API_BASE}/api/timetables/${id}`);
        if (!res.ok) throw new Error('Timetable not found');
        const tt = await res.json();

        document.getElementById('timetable-modal-title').innerText = 'Edit Timetable #' + id;
        document.getElementById('timetable-id').value = tt.id;
        document.getElementById('tt-teacher').value = tt.teacher ? tt.teacher.id : '';
        document.getElementById('tt-course').value = tt.course ? tt.course.id : '';
        document.getElementById('tt-classroom').value = tt.classroom ? tt.classroom.id : '';
        document.getElementById('tt-day').value = tt.day;
        document.getElementById('tt-start-time').value = formatTime(tt.startTime);
        document.getElementById('tt-end-time').value = formatTime(tt.endTime);
        document.getElementById('tt-auto').checked = tt.autoGenerated;

        openModal('modal-timetable');
    } catch (e) {
        showToast('Error', e.message, 'error');
    }
}

async function handleTimetableSubmit(event) {
    event.preventDefault();
    const id = document.getElementById('timetable-id').value;
    const teacherId = document.getElementById('tt-teacher').value;
    const courseId = document.getElementById('tt-course').value;
    const classroomId = document.getElementById('tt-classroom').value;
    const day = document.getElementById('tt-day').value;
    const startTime = document.getElementById('tt-start-time').value;
    const endTime = document.getElementById('tt-end-time').value;
    const autoGenerated = document.getElementById('tt-auto').checked;

    if (startTime >= endTime) {
        showToast('Validation Error', 'Start time must be before end time.', 'error');
        return;
    }

    const payload = {
        day: day,
        startTime: startTime,
        endTime: endTime,
        autoGenerated: autoGenerated,
        teacherId: teacherId ? parseInt(teacherId) : null,
        courseId: courseId ? parseInt(courseId) : null,
        classroomId: classroomId ? parseInt(classroomId) : null
    };

    try {
        let res;
        if (id) {
            // PUT /api/timetables/{id}
            res = await fetch(`${API_BASE}/api/timetables/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        } else {
            // POST /api/timetables/teacher/{teacherId}/course/{courseId}/classroom/{classroomId}
            res = await fetch(`${API_BASE}/api/timetables/teacher/${teacherId}/course/${courseId}/classroom/${classroomId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        }

        const data = await res.json();

        if (res.status === 409) {
            showToast('Conflict Detected', data.message || 'Timetable conflict detected', 'conflict');
            return;
        }

        if (!res.ok) {
            showToast('Error', data.message || 'Failed to save timetable', 'error');
            return;
        }

        showToast('Success', id ? 'Timetable updated successfully!' : 'Timetable created successfully!', 'success');
        closeModal('modal-timetable');
        await loadAllData();
    } catch (e) {
        showToast('Error', e.message, 'error');
    }
}

async function deleteTimetable(id) {
    if (!confirm(`Are you sure you want to delete Timetable #${id}?`)) return;

    try {
        const res = await fetch(`${API_BASE}/api/timetables/${id}`, {
            method: 'DELETE'
        });

        if (res.status === 404) {
            showToast('Not Found', 'Timetable not found', 'error');
            return;
        }

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to delete timetable');
        }

        showToast('Success', 'Timetable deleted successfully', 'success');
        await loadAllData();
    } catch (e) {
        showToast('Error', e.message, 'error');
    }
}

async function triggerAutoGenerate() {
    showToast('AI Scheduler', 'Running constraint solver to generate conflict-free timetable...', 'warning');

    try {
        const res = await fetch(`${API_BASE}/api/timetables/generate`, {
            method: 'POST'
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to generate timetable');
        }

        const generated = await res.json();
        showToast('Generation Complete', `Successfully generated ${generated.length} conflict-free class slots!`, 'success');
        await loadAllData();
        navigateTo('weekly');
    } catch (e) {
        showToast('Generation Failed', e.message, 'error');
    }
}

function viewTimetableDetails(id) {
    const entry = state.timetables.find(t => t.id === id);
    if (!entry) return;

    const body = document.getElementById('view-details-body');
    document.getElementById('view-details-title').innerText = `Timetable Entry #${entry.id}`;
    body.innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 12px;">
            <div><strong>Day:</strong> ${entry.day}</div>
            <div><strong>Time:</strong> ${formatTime(entry.startTime)} - ${formatTime(entry.endTime)}</div>
            <div><strong>Course:</strong> ${entry.course ? `${entry.course.courseCode} - ${entry.course.courseName} (${entry.course.credits} Credits)` : 'None'}</div>
            <div><strong>Teacher:</strong> ${entry.teacher ? `${entry.teacher.name} (${entry.teacher.department})` : 'None'}</div>
            <div><strong>Classroom:</strong> ${entry.classroom ? `${entry.classroom.roomNumber} - ${entry.classroom.building} (Capacity: ${entry.classroom.capacity})` : 'None'}</div>
            <div><strong>Scheduling Mode:</strong> <span class="badge ${entry.autoGenerated ? 'badge-auto' : 'badge-manual'}">${entry.autoGenerated ? 'Auto Generated' : 'Manual'}</span></div>
        </div>
    `;
    openModal('modal-view-details');
}

// -------------------------------------------------------------
// STUDENTS MANAGEMENT
// -------------------------------------------------------------
function renderStudentsTable() {
    const tbody = document.getElementById('students-tbody');
    if (!tbody) return;

    if (state.students.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No students found.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.students.map(s => `
        <tr>
            <td><strong>#${s.id}</strong></td>
            <td><strong>${s.name}</strong></td>
            <td>${s.email}</td>
            <td><span class="badge badge-info">${s.department}</span></td>
            <td>Year ${s.year}</td>
            <td>
                <div style="display: flex; gap: 6px;">
                    <button class="btn btn-outline btn-sm" onclick="viewStudentDetails(${s.id})">View</button>
                    <button class="btn btn-secondary btn-sm" onclick="editStudent(${s.id})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteStudent(${s.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openStudentModal() {
    document.getElementById('student-modal-title').innerText = 'Add Student';
    document.getElementById('student-id').value = '';
    document.getElementById('form-student').reset();
    openModal('modal-student');
}

function editStudent(id) {
    const student = state.students.find(s => s.id === id);
    if (!student) return;

    document.getElementById('student-modal-title').innerText = 'Edit Student #' + id;
    document.getElementById('student-id').value = student.id;
    document.getElementById('student-name').value = student.name;
    document.getElementById('student-email').value = student.email;
    document.getElementById('student-dept').value = student.department;
    document.getElementById('student-year').value = student.year;
    openModal('modal-student');
}

async function handleStudentSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('student-id').value;
    const payload = {
        name: document.getElementById('student-name').value,
        email: document.getElementById('student-email').value,
        department: document.getElementById('student-dept').value,
        year: parseInt(document.getElementById('student-year').value)
    };

    try {
        const url = id ? `${API_BASE}/api/students/${id}` : `${API_BASE}/api/students`;
        const method = id ? 'PUT' : 'POST';
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Operation failed');

        showToast('Success', id ? 'Student updated successfully' : 'Student created successfully', 'success');
        closeModal('modal-student');
        await fetchStudents();
        updateDashboardStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

async function deleteStudent(id) {
    if (!confirm('Are you sure you want to delete this student?')) return;
    try {
        const res = await fetch(`${API_BASE}/api/students/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete student');
        showToast('Success', 'Student deleted successfully', 'success');
        await fetchStudents();
        updateDashboardStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

function viewStudentDetails(id) {
    const student = state.students.find(s => s.id === id);
    if (!student) return;
    document.getElementById('view-details-title').innerText = `Student: ${student.name}`;
    document.getElementById('view-details-body').innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 10px;">
            <div><strong>ID:</strong> #${student.id}</div>
            <div><strong>Name:</strong> ${student.name}</div>
            <div><strong>Email:</strong> ${student.email}</div>
            <div><strong>Department:</strong> ${student.department}</div>
            <div><strong>Academic Year:</strong> Year ${student.year}</div>
        </div>
    `;
    openModal('modal-view-details');
}

// -------------------------------------------------------------
// TEACHERS MANAGEMENT
// -------------------------------------------------------------
function renderTeachersTable() {
    const tbody = document.getElementById('teachers-tbody');
    if (!tbody) return;

    if (state.teachers.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-state">No teachers found.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.teachers.map(t => `
        <tr>
            <td><strong>#${t.id}</strong></td>
            <td><strong>${t.name}</strong></td>
            <td>${t.subject}</td>
            <td><span class="badge badge-info">${t.department}</span></td>
            <td>
                <div style="display: flex; gap: 6px;">
                    <button class="btn btn-outline btn-sm" onclick="viewTeacherDetails(${t.id})">View</button>
                    <button class="btn btn-secondary btn-sm" onclick="editTeacher(${t.id})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteTeacher(${t.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openTeacherModal() {
    document.getElementById('teacher-modal-title').innerText = 'Add Teacher';
    document.getElementById('teacher-id').value = '';
    document.getElementById('form-teacher').reset();
    openModal('modal-teacher');
}

function editTeacher(id) {
    const teacher = state.teachers.find(t => t.id === id);
    if (!teacher) return;

    document.getElementById('teacher-modal-title').innerText = 'Edit Teacher #' + id;
    document.getElementById('teacher-id').value = teacher.id;
    document.getElementById('teacher-name').value = teacher.name;
    document.getElementById('teacher-subject').value = teacher.subject;
    document.getElementById('teacher-dept').value = teacher.department;
    openModal('modal-teacher');
}

async function handleTeacherSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('teacher-id').value;
    const payload = {
        name: document.getElementById('teacher-name').value,
        subject: document.getElementById('teacher-subject').value,
        department: document.getElementById('teacher-dept').value
    };

    try {
        const url = id ? `${API_BASE}/api/teachers/${id}` : `${API_BASE}/api/teachers`;
        const method = id ? 'PUT' : 'POST';
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Operation failed');

        showToast('Success', id ? 'Teacher updated successfully' : 'Teacher created successfully', 'success');
        closeModal('modal-teacher');
        await fetchTeachers();
        populateDropdowns();
        updateDashboardStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

async function deleteTeacher(id) {
    if (!confirm('Are you sure you want to delete this teacher?')) return;
    try {
        const res = await fetch(`${API_BASE}/api/teachers/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete teacher');
        showToast('Success', 'Teacher deleted successfully', 'success');
        await fetchTeachers();
        populateDropdowns();
        updateDashboardStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

function viewTeacherDetails(id) {
    const teacher = state.teachers.find(t => t.id === id);
    if (!teacher) return;
    document.getElementById('view-details-title').innerText = `Teacher: ${teacher.name}`;
    document.getElementById('view-details-body').innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 10px;">
            <div><strong>ID:</strong> #${teacher.id}</div>
            <div><strong>Name:</strong> ${teacher.name}</div>
            <div><strong>Subject:</strong> ${teacher.subject}</div>
            <div><strong>Department:</strong> ${teacher.department}</div>
        </div>
    `;
    openModal('modal-view-details');
}

// -------------------------------------------------------------
// COURSES MANAGEMENT
// -------------------------------------------------------------
function renderCoursesTable() {
    const tbody = document.getElementById('courses-tbody');
    if (!tbody) return;

    if (state.courses.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No courses found.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.courses.map(c => `
        <tr>
            <td><strong>#${c.id}</strong></td>
            <td><span class="badge badge-info">${c.courseCode}</span></td>
            <td><strong>${c.courseName}</strong></td>
            <td>${c.department}</td>
            <td>${c.credits} Credits</td>
            <td>
                <div style="display: flex; gap: 6px;">
                    <button class="btn btn-outline btn-sm" onclick="viewCourseDetails(${c.id})">View</button>
                    <button class="btn btn-secondary btn-sm" onclick="editCourse(${c.id})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteCourse(${c.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openCourseModal() {
    document.getElementById('course-modal-title').innerText = 'Add Course';
    document.getElementById('course-id').value = '';
    document.getElementById('form-course').reset();
    openModal('modal-course');
}

function editCourse(id) {
    const course = state.courses.find(c => c.id === id);
    if (!course) return;

    document.getElementById('course-modal-title').innerText = 'Edit Course #' + id;
    document.getElementById('course-id').value = course.id;
    document.getElementById('course-code').value = course.courseCode;
    document.getElementById('course-name').value = course.courseName;
    document.getElementById('course-dept').value = course.department;
    document.getElementById('course-credits').value = course.credits;
    openModal('modal-course');
}

async function handleCourseSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('course-id').value;
    const payload = {
        courseCode: document.getElementById('course-code').value,
        courseName: document.getElementById('course-name').value,
        department: document.getElementById('course-dept').value,
        credits: parseInt(document.getElementById('course-credits').value)
    };

    try {
        const url = id ? `${API_BASE}/api/courses/${id}` : `${API_BASE}/api/courses`;
        const method = id ? 'PUT' : 'POST';
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Operation failed');

        showToast('Success', id ? 'Course updated successfully' : 'Course created successfully', 'success');
        closeModal('modal-course');
        await fetchCourses();
        populateDropdowns();
        updateDashboardStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

async function deleteCourse(id) {
    if (!confirm('Are you sure you want to delete this course?')) return;
    try {
        const res = await fetch(`${API_BASE}/api/courses/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete course');
        showToast('Success', 'Course deleted successfully', 'success');
        await fetchCourses();
        populateDropdowns();
        updateDashboardStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

function viewCourseDetails(id) {
    const course = state.courses.find(c => c.id === id);
    if (!course) return;
    document.getElementById('view-details-title').innerText = `Course: ${course.courseCode}`;
    document.getElementById('view-details-body').innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 10px;">
            <div><strong>ID:</strong> #${course.id}</div>
            <div><strong>Code:</strong> ${course.courseCode}</div>
            <div><strong>Name:</strong> ${course.courseName}</div>
            <div><strong>Department:</strong> ${course.department}</div>
            <div><strong>Credits:</strong> ${course.credits} Credits</div>
        </div>
    `;
    openModal('modal-view-details');
}

// -------------------------------------------------------------
// CLASSROOMS MANAGEMENT
// -------------------------------------------------------------
function renderClassroomsTable() {
    const tbody = document.getElementById('classrooms-tbody');
    if (!tbody) return;

    if (state.classrooms.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-state">No classrooms found.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.classrooms.map(c => `
        <tr>
            <td><strong>#${c.id}</strong></td>
            <td>${c.building}</td>
            <td><strong>${c.roomNumber}</strong></td>
            <td>${c.capacity} Seats</td>
            <td>
                <div style="display: flex; gap: 6px;">
                    <button class="btn btn-outline btn-sm" onclick="viewClassroomDetails(${c.id})">View</button>
                    <button class="btn btn-secondary btn-sm" onclick="editClassroom(${c.id})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteClassroom(${c.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openClassroomModal() {
    document.getElementById('classroom-modal-title').innerText = 'Add Classroom';
    document.getElementById('classroom-id').value = '';
    document.getElementById('form-classroom').reset();
    openModal('modal-classroom');
}

function editClassroom(id) {
    const classroom = state.classrooms.find(c => c.id === id);
    if (!classroom) return;

    document.getElementById('classroom-modal-title').innerText = 'Edit Classroom #' + id;
    document.getElementById('classroom-id').value = classroom.id;
    document.getElementById('classroom-bldg').value = classroom.building;
    document.getElementById('classroom-room').value = classroom.roomNumber;
    document.getElementById('classroom-cap').value = classroom.capacity;
    openModal('modal-classroom');
}

async function handleClassroomSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('classroom-id').value;
    const payload = {
        building: document.getElementById('classroom-bldg').value,
        roomNumber: document.getElementById('classroom-room').value,
        capacity: parseInt(document.getElementById('classroom-cap').value)
    };

    try {
        const url = id ? `${API_BASE}/api/classrooms/${id}` : `${API_BASE}/api/classrooms`;
        const method = id ? 'PUT' : 'POST';
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Operation failed');

        showToast('Success', id ? 'Classroom updated successfully' : 'Classroom created successfully', 'success');
        closeModal('modal-classroom');
        await fetchClassrooms();
        populateDropdowns();
        updateDashboardStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

async function deleteClassroom(id) {
    if (!confirm('Are you sure you want to delete this classroom?')) return;
    try {
        const res = await fetch(`${API_BASE}/api/classrooms/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete classroom');
        showToast('Success', 'Classroom deleted successfully', 'success');
        await fetchClassrooms();
        populateDropdowns();
        updateDashboardStats();
    } catch (err) {
        showToast('Error', err.message, 'error');
    }
}

function viewClassroomDetails(id) {
    const classroom = state.classrooms.find(c => c.id === id);
    if (!classroom) return;
    document.getElementById('view-details-title').innerText = `Classroom: ${classroom.roomNumber}`;
    document.getElementById('view-details-body').innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 10px;">
            <div><strong>ID:</strong> #${classroom.id}</div>
            <div><strong>Building:</strong> ${classroom.building}</div>
            <div><strong>Room Number:</strong> ${classroom.roomNumber}</div>
            <div><strong>Capacity:</strong> ${classroom.capacity} Students</div>
        </div>
    `;
    openModal('modal-view-details');
}

// -------------------------------------------------------------
// UTILITIES (Modals, Toasts, Formatting)
// -------------------------------------------------------------
function openModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.add('active');
}

function closeModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.remove('active');
}

function formatTime(timeStr) {
    if (!timeStr) return '';
    if (typeof timeStr === 'string' && timeStr.length >= 5) {
        return timeStr.substring(0, 5);
    }
    return timeStr;
}

function showToast(title, message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <h5>${title}</h5>
            <p>${message}</p>
        </div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}


// ---------------------------------------------------------------
// EXPORT & UTILITIES (appended)
// ---------------------------------------------------------------

/**
 * Export all timetable entries to a CSV file.
 */
function exportWeeklyToCSV() {
    const rows = [
        ['ID', 'Day', 'Start Time', 'End Time', 'Teacher', 'Course Code', 'Course Name', 'Classroom', 'Building', 'Capacity', 'Type']
    ];

    state.timetables.forEach(t => {
        rows.push([
            t.id,
            t.day,
            formatTime(t.startTime),
            formatTime(t.endTime),
            t.teacher    ? t.teacher.name             : '',
            t.course     ? t.course.courseCode         : '',
            t.course     ? t.course.courseName         : '',
            t.classroom  ? t.classroom.roomNumber      : '',
            t.classroom  ? t.classroom.building        : '',
            t.classroom  ? t.classroom.capacity        : '',
            t.autoGenerated ? 'Auto Generated' : 'Manual'
        ]);
    });

    const csv = rows.map(row =>
        row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(',')
    ).join('\r\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url  = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `timetable_export_${new Date().toISOString().slice(0,10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    showToast('Export Complete', `Exported ${state.timetables.length} timetable records to CSV.`, 'success');
}

/**
 * Delete all auto-generated timetables via DELETE /api/timetables/auto-generated.
 */
async function clearAutoGeneratedTimetables() {
    if (!confirm('Delete ALL auto-generated timetables? This cannot be undone.')) return;

    try {
        const res = await fetch(`${API_BASE}/api/timetables/auto-generated`, { method: 'DELETE' });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Failed to clear auto-generated timetables');
        }

        showToast('Success', 'All auto-generated timetables removed.', 'success');
        await loadAllData();
    } catch (e) {
        showToast('Error', e.message, 'error');
    }
}

/**
 * Render an inline summary row beneath the timetable table.
 */
function renderTimetablesSummaryBar() {
    const existing = document.getElementById('tt-summary-bar');
    if (existing) existing.remove();

    const total  = state.timetables.length;
    const manual = state.timetables.filter(t => !t.autoGenerated).length;
    const auto   = state.timetables.filter(t =>  t.autoGenerated).length;
    const days   = [...new Set(state.timetables.map(t => t.day))].sort().join(', ') || '—';

    const bar = document.createElement('div');
    bar.id = 'tt-summary-bar';
    bar.style.cssText = `
        display:flex;gap:20px;flex-wrap:wrap;
        padding:12px 18px;background:#f8fafc;
        border:1px solid #e2e8f0;border-radius:8px;
        margin-top:12px;font-size:0.85rem;color:#475569;
    `;
    bar.innerHTML = `
        <span>📋 <strong>${total}</strong> total</span>
        <span>✏️  <strong>${manual}</strong> manual</span>
        <span>⚡ <strong>${auto}</strong> auto-generated</span>
        <span>📅 Days: <strong>${days}</strong></span>
    `;

    const tableWrap = document.querySelector('#section-timetables .table-responsive');
    if (tableWrap) tableWrap.insertAdjacentElement('afterend', bar);
}

// Re-hook renderTimetablesTable so it always refreshes the summary bar
const _baseTTRender = renderTimetablesTable;

/**
 * Close any open modal when the backdrop itself is clicked.
 */
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-backdrop')) {
        e.target.classList.remove('active');
    }
});

/**
 * Keyboard shortcuts:
 *   n       → open Add Timetable modal
 *   Escape  → close the top-most open modal
 */
document.addEventListener('keydown', (e) => {
    const active = document.activeElement.tagName;
    if (active === 'INPUT' || active === 'TEXTAREA' || active === 'SELECT') return;

    if (e.key === 'n' || e.key === 'N') {
        openCreateTimetableModal();
    }
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-backdrop.active')
            .forEach(m => m.classList.remove('active'));
    }
});
