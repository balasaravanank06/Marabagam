export interface BackendHealthResponse {
  status: string;
  service: string;
  timestamp: string;
  database: {
    connected: boolean;
    databaseName: string;
  };
}

export interface AiHealthResponse {
  status: string;
  service: string;
}

export interface SystemHealthState {
  backend: boolean | null;
  aiService: boolean | null;
  database: boolean | null;
  loading: boolean;
  error?: string;
}
