import { Certificate, VerificationResponse } from '../types/certificate';

const API_BASE = '/api/v1/certificates';

function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

export const certificateService = {
  async getUserCertificates(): Promise<Certificate[]> {
    const res = await fetch(API_BASE, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error('Failed to fetch certificates');
    const json = await res.json();
    return json.data;
  },

  async getCertificateById(id: string): Promise<Certificate> {
    const res = await fetch(`${API_BASE}/${id}`, { headers: getAuthHeaders() });
    if (!res.ok) throw new Error('Failed to fetch certificate');
    const json = await res.json();
    return json.data;
  },

  async verifyCertificate(code: string): Promise<VerificationResponse> {
    const res = await fetch(`${API_BASE}/verify/${code}`);
    if (!res.ok) throw new Error('Failed to verify certificate');
    const json = await res.json();
    return json.data;
  }
};
