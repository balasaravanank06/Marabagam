import { useState, useEffect } from 'react';
import { healthService } from '../services/healthService';
import { SystemHealthState } from '../types';

export function useHealth(): SystemHealthState {
  const [state, setState] = useState<SystemHealthState>({
    backend: null,
    aiService: null,
    database: null,
    loading: true,
  });

  useEffect(() => {
    let isMounted = true;

    async function checkHealth() {
      try {
        const [backendRes, aiRes] = await Promise.allSettled([
          healthService.getBackendHealth(),
          healthService.getAiHealth(),
        ]);

        if (!isMounted) return;

        const isBackendUp = backendRes.status === 'fulfilled' && backendRes.value.status === 'UP';
        const isDbUp = backendRes.status === 'fulfilled' && backendRes.value.database?.connected === true;
        const isAiUp = aiRes.status === 'fulfilled' && aiRes.value.status === 'UP';

        setState({
          backend: isBackendUp,
          database: isDbUp,
          aiService: isAiUp,
          loading: false,
        });
      } catch (err) {
        if (!isMounted) return;
        setState({
          backend: false,
          database: false,
          aiService: false,
          loading: false,
          error: (err as Error).message,
        });
      }
    }

    checkHealth();
    const interval = setInterval(checkHealth, 15000);
    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, []);

  return state;
}
