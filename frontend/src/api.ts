import { mockData } from './data/mockData';

const API_BASE_URL = 'http://localhost:7070/api';

// Generic API Response type
export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

// Types for API requests/responses
export interface SignupRequest {
    name: string;
    email: string;
    password: string;
    companyName?: string;
    businessRegistrationNumber?: string;
    phoneNumber?: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface User {
    id: number;
    name: string;
    email: string;
    role?: string;
}

export interface AuthResponse extends User {
    message: string;
}

export interface PartnershipInquiryRequest {
    company: string;
    companySize: string;
    manager: string;
    title?: string;
    phone: string;
    email: string;
    programType?: string;
    participants?: string;
    preferredDate?: string;
    budget?: string;
    message?: string;
}

// Content API
export const fetchContent = async () => {
    try {
        // Check if we are in production mode (Vite build)
        if (import.meta.env.PROD) {
            return mockData;
        }

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 3000);

        try {
            const response = await fetch(`${API_BASE_URL}/content`, {
                signal: controller.signal
            });
            clearTimeout(timeoutId);

            const result: ApiResponse<any> = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || 'Network response was not ok');
            }
            return result.data;
        } catch (error) {
            clearTimeout(timeoutId);
            throw error;
        }


    } catch (error) {
        console.warn('Backend unavailable, using mock data:', error);
        // Fallback to mock data when backend is not available
        return mockData;
    }
};

// Authentication APIs
export const signup = async (data: SignupRequest): Promise<AuthResponse> => {
    const response = await fetch(`${API_BASE_URL}/auth/signup`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    });

    const result: ApiResponse<User> = await response.json();

    if (!response.ok || !result.success) {
        throw new Error(result.message || 'Signup failed');
    }

    return {
        ...result.data,
        message: result.message
    };
};

export const login = async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    });

    const result: ApiResponse<User> = await response.json();

    if (!response.ok || !result.success) {
        throw new Error(result.message || 'Login failed');
    }

    return {
        ...result.data,
        message: result.message
    };
};

export const logout = async (): Promise<void> => {
    const response = await fetch(`${API_BASE_URL}/auth/logout`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
    });

    const result: ApiResponse<void> = await response.json();

    if (!response.ok || !result.success) {
        throw new Error(result.message || 'Logout failed');
    }
};

// Partnership API
export const submitPartnershipInquiry = async (data: PartnershipInquiryRequest): Promise<ApiResponse<any>> => {
    const response = await fetch(`${API_BASE_URL}/partnership/inquiry`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    });

    const result: ApiResponse<any> = await response.json();

    if (!response.ok || !result.success) {
        throw new Error(result.message || 'Failed to submit inquiry');
    }

    return result;
};
