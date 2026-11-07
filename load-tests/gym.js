import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    ramping_api_load: {
      executor: 'ramping-vus',
      startVUs: 1,
      stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 25 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<750', 'p(99)<1500'],
  },
};

export function setup() {
  const email = `load-${Date.now()}@example.test`;
  const registration = http.post(
    `${baseUrl}/api/auth/register`,
    JSON.stringify({ name: 'Load', last_name: 'Runner', email, pass: 'load-test-password' }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(registration, { 'load-test user is created': (response) => response.status === 200 });
  return { token: registration.json('access_token') };
}

export default function (data) {
  const health = http.get(`${baseUrl}/actuator/health`);
  check(health, { 'health endpoint is up': (response) => response.status === 200 });

  const response = http.get(`${baseUrl}/api/users`, {
    headers: { Authorization: `Bearer ${data.token}` },
  });
  check(response, {
    'authenticated database read succeeds': (result) => result.status === 200,
  });
  sleep(1);
}
