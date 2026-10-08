const STORAGE_KEY = 'finance-manager-web-v1';
const CATEGORIES = ['SALARY', 'FOOD', 'HOUSING', 'TRANSPORT', 'UTILITIES', 'HEALTH', 'EDUCATION', 'ENTERTAINMENT', 'OTHER'];

const state = {
  users: [],
  transactions: [],
  budgets: {},
  currentUser: null,
};

const selectors = {
  authPanel: document.getElementById('authPanel'),
  appPanel: document.getElementById('appPanel'),
  logoutBtn: document.getElementById('logoutBtn'),
  authForm: document.getElementById('authForm'),
  username: document.getElementById('username'),
  password: document.getElementById('password'),
  registerBtn: document.getElementById('registerBtn'),
  transactionForm: document.getElementById('transactionForm'),
  transactionType: document.getElementById('transactionType'),
  amount: document.getElementById('amount'),
  category: document.getElementById('category'),
  transactionDate: document.getElementById('transactionDate'),
  description: document.getElementById('description'),
  budgetForm: document.getElementById('budgetForm'),
  budgetCategory: document.getElementById('budgetCategory'),
  budgetMonth: document.getElementById('budgetMonth'),
  budgetLimit: document.getElementById('budgetLimit'),
  filterMonth: document.getElementById('filterMonth'),
  transactionList: document.getElementById('transactionList'),
  budgetStatus: document.getElementById('budgetStatus'),
  reportSummary: document.getElementById('reportSummary'),
  categoryBreakdown: document.getElementById('categoryBreakdown'),
  incomeSummary: document.getElementById('incomeSummary'),
  expenseSummary: document.getElementById('expenseSummary'),
  netSummary: document.getElementById('netSummary'),
};

function initialize() {
  loadState();
  populateCategoryOptions();
  selectors.transactionDate.value = todayISO();
  selectors.budgetMonth.value = currentMonthISO();
  selectors.filterMonth.value = currentMonthISO();

  selectors.authForm.addEventListener('submit', (event) => {
    event.preventDefault();
    loginUser();
  });

  selectors.registerBtn.addEventListener('click', () => {
    const username = selectors.username.value.trim();
    const password = selectors.password.value.trim();
    registerUser(username, password);
  });

  selectors.transactionForm.addEventListener('submit', (event) => {
    event.preventDefault();
    addTransaction();
  });

  selectors.budgetForm.addEventListener('submit', (event) => {
    event.preventDefault();
    setBudget();
  });

  selectors.filterMonth.addEventListener('input', render);
  selectors.logoutBtn.addEventListener('click', logout);

  render();
}

function loadState() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    state.users = [];
    state.transactions = [];
    state.budgets = {};
    return;
  }

  try {
    const parsed = JSON.parse(raw);
    state.users = parsed.users || [];
    state.transactions = parsed.transactions || [];
    state.budgets = parsed.budgets || {};
  } catch (error) {
    console.error('Could not read storage', error);
    state.users = [];
    state.transactions = [];
    state.budgets = {};
  }
}

function saveState() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({
    users: state.users,
    transactions: state.transactions,
    budgets: state.budgets,
  }));
}

function populateCategoryOptions() {
  const labels = [...CATEGORIES];
  selectors.category.innerHTML = labels.map((category) => `<option value="${category}">${category}</option>`).join('');
  selectors.budgetCategory.innerHTML = labels.map((category) => `<option value="${category}">${category}</option>`).join('');
}

function loginUser() {
  const username = selectors.username.value.trim();
  const password = selectors.password.value.trim();

  if (!username || !password) {
    alert('Username and password are required.');
    return;
  }

  const user = state.users.find((entry) => entry.username.toLowerCase() === username.toLowerCase() && entry.password === password);
  if (!user) {
    alert('Invalid username or password.');
    return;
  }

  state.currentUser = user;
  render();
}

function registerUser(username, password) {
  if (username.length < 3 || password.length < 4) {
    alert('Username must be at least 3 characters and password at least 4 characters.');
    return;
  }

  const exists = state.users.some((user) => user.username.toLowerCase() === username.toLowerCase());
  if (exists) {
    alert('This username already exists.');
    return;
  }

  const user = {
    id: Date.now(),
    username,
    password,
  };

  state.users.push(user);
  state.currentUser = user;
  saveState();
  render();
}

function logout() {
  state.currentUser = null;
  selectors.username.value = '';
  selectors.password.value = '';
  render();
}

function addTransaction() {
  if (!state.currentUser) return;

  const type = selectors.transactionType.value;
  const amount = Number(selectors.amount.value);
  const category = selectors.category.value;
  const date = selectors.transactionDate.value || todayISO();
  const description = selectors.description.value.trim();

  if (!amount || amount <= 0) {
    alert('Amount must be greater than zero.');
    return;
  }

  state.transactions.push({
    id: Date.now(),
    userId: state.currentUser.id,
    type,
    amount: Number(amount.toFixed(2)),
    category,
    date,
    description,
  });

  saveState();
  selectors.transactionForm.reset();
  selectors.transactionDate.value = todayISO();
  render();
}

function setBudget() {
  if (!state.currentUser) return;

  const category = selectors.budgetCategory.value;
  const month = selectors.budgetMonth.value;
  const limit = Number(selectors.budgetLimit.value);

  if (!month || !limit || limit <= 0) {
    alert('Please provide a valid month and limit.');
    return;
  }

  const key = `${state.currentUser.id}:${month}`;
  state.budgets[key] = { ...state.budgets[key], [category]: Number(limit.toFixed(2)) };
  saveState();
  selectors.budgetForm.reset();
  selectors.budgetMonth.value = currentMonthISO();
  render();
}

function deleteTransaction(id) {
  if (!state.currentUser) return;
  state.transactions = state.transactions.filter((entry) => entry.id !== id || entry.userId !== state.currentUser.id);
  saveState();
  render();
}

function render() {
  const loggedIn = !!state.currentUser;
  selectors.authPanel.classList.toggle('hidden', loggedIn);
  selectors.appPanel.classList.toggle('hidden', !loggedIn);
  selectors.logoutBtn.classList.toggle('hidden', !loggedIn);

  if (!loggedIn) {
    return;
  }

  const monthFilter = selectors.filterMonth.value || currentMonthISO().slice(0, 7);
  const userTransactions = state.transactions.filter((entry) => entry.userId === state.currentUser.id);
  const filteredTransactions = userTransactions.filter((entry) => entry.date.startsWith(monthFilter));

  renderSummary(userTransactions);
  renderTransactions(filteredTransactions);
  renderBudgets(monthFilter, userTransactions);
  renderReport(monthFilter, userTransactions);
}

function renderSummary(userTransactions) {
  const income = userTransactions.filter((entry) => entry.type === 'income').reduce((sum, entry) => sum + entry.amount, 0);
  const expense = userTransactions.filter((entry) => entry.type === 'expense').reduce((sum, entry) => sum + entry.amount, 0);
  const net = income - expense;

  selectors.incomeSummary.textContent = money(income);
  selectors.expenseSummary.textContent = money(expense);
  selectors.netSummary.textContent = money(net);
}

function renderTransactions(transactions) {
  if (!transactions.length) {
    selectors.transactionList.innerHTML = '<div class="transaction-row"><span>No transactions for this month.</span></div>';
    return;
  }

  const allTransactions = [...transactions].sort((a, b) => new Date(b.date) - new Date(a.date));
  let runningBalance = 0;
  const rows = allTransactions.map((entry) => {
    runningBalance += entry.type === 'income' ? entry.amount : -entry.amount;
    return `
      <div class="transaction-row">
        <span>${entry.date}</span>
        <span>${entry.category}</span>
        <span>${entry.description || '—'}</span>
        <span class="amount ${entry.type === 'income' ? 'income' : 'expense'}">${entry.type === 'income' ? '+' : '-'}${money(entry.amount)}</span>
        <span>${money(runningBalance)}</span>
        <button class="delete-btn" data-id="${entry.id}">Delete</button>
      </div>
    `;
  }).join('');

  selectors.transactionList.innerHTML = rows;
  selectors.transactionList.querySelectorAll('.delete-btn').forEach((button) => {
    button.addEventListener('click', () => deleteTransaction(Number(button.dataset.id)));
  });
}

function renderBudgets(month, userTransactions) {
  const monthBudgetEntries = Object.entries(state.budgets)
    .filter(([key]) => key.startsWith(`${state.currentUser.id}:`))
    .map(([key, value]) => ({ key, month: key.split(':')[1], budget: value }))
    .filter((entry) => entry.month === month);

  if (!monthBudgetEntries.length) {
    selectors.budgetStatus.innerHTML = '<div class="budget-item">No budgets set for this month.</div>';
    return;
  }

  const output = monthBudgetEntries.flatMap((entry) => {
    return Object.entries(entry.budget).map(([category, limit]) => {
      const spent = userTransactions
        .filter((row) => row.category === category && row.date.startsWith(month) && row.type === 'expense')
        .reduce((sum, row) => sum + row.amount, 0);

      const remaining = limit - spent;
      const percent = Math.min((spent / limit) * 100, 100);
      const over = spent > limit;

      return `
        <div class="budget-item ${over ? 'over' : ''}">
          <strong>${category}</strong>
          <div>Spent ${money(spent)} / ${money(limit)} (${over ? 'Over by' : 'Remaining'} ${money(Math.abs(remaining))})</div>
          <span class="progress"><span class="progress-bar" style="width: ${percent}%"></span></span>
        </div>
      `;
    });
  }).join('');

  selectors.budgetStatus.innerHTML = output;
}

function renderReport(month, userTransactions) {
  const monthTransactions = userTransactions.filter((entry) => entry.date.startsWith(month));
  const income = monthTransactions.filter((row) => row.type === 'income').reduce((sum, row) => sum + row.amount, 0);
  const expense = monthTransactions.filter((row) => row.type === 'expense').reduce((sum, row) => sum + row.amount, 0);
  const net = income - expense;

  selectors.reportSummary.innerHTML = `
    <div class="report-line"><strong>Income:</strong> ${money(income)}</div>
    <div class="report-line"><strong>Expense:</strong> ${money(expense)}</div>
    <div class="report-line"><strong>Net:</strong> ${money(net)}</div>
  `;

  const categoryTotals = {};
  monthTransactions.filter((row) => row.type === 'expense').forEach((row) => {
    categoryTotals[row.category] = (categoryTotals[row.category] || 0) + row.amount;
  });

  const entries = Object.entries(categoryTotals).sort((a, b) => b[1] - a[1]);

  if (!entries.length) {
    selectors.categoryBreakdown.innerHTML = '<div class="category-item">No expense data for this month.</div>';
    return;
  }

  const maxValue = Math.max(...entries.map(([, value]) => value));
  selectors.categoryBreakdown.innerHTML = entries.map(([category, total]) => {
    const width = (total / maxValue) * 100;
    return `
      <div class="category-item">
        <div><strong>${category}</strong> ${money(total)}</div>
        <span class="progress"><span class="progress-bar" style="width: ${width}%"></span></span>
      </div>
    `;
  }).join('');
}

function money(value) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value || 0);
}

function todayISO() {
  return new Date().toISOString().slice(0, 10);
}

function currentMonthISO() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

initialize();
