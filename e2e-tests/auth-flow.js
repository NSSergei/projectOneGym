import http from 'k6/http';
import { check } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    http_req_failed: ['rate==0'],
    http_req_duration: ['p(95)<1000'],
  },
};

export default function () {
  const suffix = `${Date.now()}-${__VU}-${__ITER}`;
  const email = `e2e-${suffix}@example.test`;
  const password = 'e2e-test-password';
  const registration = http.post(
    `${baseUrl}/api/auth/register`,
    JSON.stringify({ name: 'E2E', last_name: 'Runner', email, pass: password }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(registration, {
    'registration creates a client': (response) => response.status === 200,
    'registration returns an access token': (response) => Boolean(response.json('access_token')),
  });

  const login = http.post(
    `${baseUrl}/api/auth/login`,
    JSON.stringify({ email, pass: password }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  const token = login.json('access_token');
  check(login, {
    'login succeeds': (response) => response.status === 200,
    'login returns an access token': () => Boolean(token),
  });

  if (token) {
    const users = http.get(`${baseUrl}/api/users`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    check(users, {
      'JWT authorizes a protected API route': (response) => response.status === 200,
      'registered user is persisted': (response) => response.json().some((user) => user.email === email),
    });
  }
}
