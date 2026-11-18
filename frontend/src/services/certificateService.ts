import { apiClient } from '../lib/api';
import { Certificate, VerificationResponse } from '../types/certificate';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const certificateService = {
  async getUserCertificates(): Promise<Certificate[]> {
    const res = await apiClient.get<ApiResponse<Certificate[]>>('/certificates');
    return res.data.data;
  },

  async getCertificateById(id: string): Promise<Certificate> {
    const res = await apiClient.get<ApiResponse<Certificate>>(`/certificates/${id}`);
    return res.data.data;
  },

  async verifyCertificate(code: string): Promise<VerificationResponse> {
    const res = await apiClient.get<ApiResponse<VerificationResponse>>(`/certificates/verify/${code}`);
    return res.data.data;
  }
};
