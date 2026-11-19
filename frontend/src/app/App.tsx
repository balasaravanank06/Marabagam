import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import { AppShell } from '../components/layout/AppShell';
import { DevShellPage } from '../pages/DevShellPage';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { ProfilePage } from '../pages/ProfilePage';
import { ArtFormDirectoryPage } from '../pages/ArtFormDirectoryPage';
import { ArtFormDetailPage } from '../pages/ArtFormDetailPage';
import { AdminArtFormsPage } from '../pages/AdminArtFormsPage';
import { CourseDirectoryPage } from '../pages/CourseDirectoryPage';
import { CourseDetailPage } from '../pages/CourseDetailPage';
import { LearningExperiencePage } from '../pages/LearningExperiencePage';
import { MyLearningPage } from '../pages/MyLearningPage';
import { AdminCoursesPage } from '../pages/AdminCoursesPage';
import { AiCulturalGuidePage } from '../pages/AiCulturalGuidePage';
import { HeritageMapPage } from '../pages/HeritageMapPage';
import { MovementCoachDirectoryPage } from '../pages/MovementCoachDirectoryPage';
import { MovementPracticePage } from '../pages/MovementPracticePage';
import { QuizPlayerPage } from '../pages/QuizPlayerPage';
import { AchievementsPage } from '../pages/AchievementsPage';
import { LeaderboardPage } from '../pages/LeaderboardPage';
import { CertificatesPage } from '../pages/CertificatesPage';
import { CertificateVerifyPage } from '../pages/CertificateVerifyPage';
import { EventsPage } from '../pages/EventsPage';
import { EventDetailPage } from '../pages/EventDetailPage';
import { MyEventsPage } from '../pages/MyEventsPage';
import { RecommendationsPage } from '../pages/RecommendationsPage';
import { CommunityPage } from '../pages/CommunityPage';
import { PostDetailPage } from '../pages/PostDetailPage';
import { ProtectedRoute } from '../components/common/ProtectedRoute';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppShell>
          <Routes>
            <Route path="/" element={<DevShellPage />} />
            <Route path="/art-forms" element={<ArtFormDirectoryPage />} />
            <Route path="/art-forms/:slug" element={<ArtFormDetailPage />} />
            <Route path="/courses" element={<CourseDirectoryPage />} />
            <Route path="/course/:slug" element={<CourseDetailPage />} />
            <Route path="/ai-guide" element={<AiCulturalGuidePage />} />
            <Route path="/heritage-map" element={<HeritageMapPage />} />
            <Route path="/movement-coach" element={<MovementCoachDirectoryPage />} />
            <Route
              path="/movement-coach/:movementId/practice"
              element={
                <ProtectedRoute>
                  <MovementPracticePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/quiz/:id"
              element={
                <ProtectedRoute>
                  <QuizPlayerPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/achievements"
              element={
                <ProtectedRoute>
                  <AchievementsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-achievements"
              element={
                <ProtectedRoute>
                  <AchievementsPage />
                </ProtectedRoute>
              }
            />
            <Route path="/leaderboard" element={<LeaderboardPage />} />
            <Route
              path="/certificates"
              element={
                <ProtectedRoute>
                  <CertificatesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-certificates"
              element={
                <ProtectedRoute>
                  <CertificatesPage />
                </ProtectedRoute>
              }
            />
            <Route path="/certificate/:code/verify" element={<CertificateVerifyPage />} />
            <Route path="/events" element={<EventsPage />} />
            <Route path="/event/:slug" element={<EventDetailPage />} />
            <Route
              path="/my-events"
              element={
                <ProtectedRoute>
                  <MyEventsPage />
                </ProtectedRoute>
              }
            />
            <Route path="/recommendations" element={<RecommendationsPage />} />
            <Route path="/community" element={<CommunityPage />} />
            <Route path="/community/post/:id" element={<PostDetailPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <ProfilePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-learning"
              element={
                <ProtectedRoute>
                  <MyLearningPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/course/:slug/learn"
              element={
                <ProtectedRoute>
                  <LearningExperiencePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/art-forms"
              element={
                <ProtectedRoute allowedRoles={['ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN']}>
                  <AdminArtFormsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/courses"
              element={
                <ProtectedRoute allowedRoles={['ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN']}>
                  <AdminCoursesPage />
                </ProtectedRoute>
              }
            />
          </Routes>
        </AppShell>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
