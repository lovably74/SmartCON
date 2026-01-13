/**
 * Property-Based Test: Frontend Technology Stack Consistency
 * 
 * **Property 1: Frontend Technology Stack Consistency**
 * For any frontend project configuration, the package.json should contain 
 * React 18+, TypeScript 5+, and Vite 5+ with compatible versions
 * 
 * **Validates: Requirements 1.2**
 * 
 * Feature: frontend-separation-mariadb-migration, Property 1: Frontend Technology Stack Consistency
 */

import { describe, it, expect } from 'vitest';
import { fc } from '@fast-check/vitest';
import packageJson from '../../../package.json';
import fs from 'fs';
import path from 'path';

describe('Property 1: Frontend Technology Stack Consistency', () => {
  
  it('should have React 18+ as core dependency', () => {
    fc.assert(
      fc.property(fc.constant(packageJson), (pkg) => {
        // React 18+ 버전 확인
        const reactVersion = pkg.dependencies?.react;
        expect(reactVersion).toBeDefined();
        
        // 버전 문자열에서 숫자 추출 (^18.3.1 -> 18)
        const majorVersion = parseInt(reactVersion!.replace(/[^\d.]/g, '').split('.')[0]);
        expect(majorVersion).toBeGreaterThanOrEqual(18);
        
        // React DOM도 같은 버전이어야 함
        const reactDomVersion = pkg.dependencies?.['react-dom'];
        expect(reactDomVersion).toBeDefined();
        const reactDomMajor = parseInt(reactDomVersion!.replace(/[^\d.]/g, '').split('.')[0]);
        expect(reactDomMajor).toBe(majorVersion);
        
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('should have TypeScript 5+ as dev dependency', () => {
    fc.assert(
      fc.property(fc.constant(packageJson), (pkg) => {
        // TypeScript 5+ 버전 확인
        const typescriptVersion = pkg.devDependencies?.typescript;
        expect(typescriptVersion).toBeDefined();
        
        // 버전 문자열에서 숫자 추출 (~5.9.3 -> 5)
        const majorVersion = parseInt(typescriptVersion!.replace(/[^\d.]/g, '').split('.')[0]);
        expect(majorVersion).toBeGreaterThanOrEqual(5);
        
        // @types/react와 @types/react-dom도 있어야 함
        expect(pkg.devDependencies?.['@types/react']).toBeDefined();
        expect(pkg.devDependencies?.['@types/react-dom']).toBeDefined();
        
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('should have Vite 5+ as dev dependency', () => {
    fc.assert(
      fc.property(fc.constant(packageJson), (pkg) => {
        // Vite 5+ 버전 확인
        const viteVersion = pkg.devDependencies?.vite;
        expect(viteVersion).toBeDefined();
        
        // 버전 문자열에서 숫자 추출 (^7.2.4 -> 7, 하지만 5+ 요구사항)
        const majorVersion = parseInt(viteVersion!.replace(/[^\d.]/g, '').split('.')[0]);
        expect(majorVersion).toBeGreaterThanOrEqual(5);
        
        // Vite React 플러그인도 있어야 함
        expect(pkg.devDependencies?.['@vitejs/plugin-react']).toBeDefined();
        
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('should have compatible core technology versions', () => {
    fc.assert(
      fc.property(fc.constant(packageJson), (pkg) => {
        // 모든 핵심 기술이 호환 가능한 버전인지 확인
        const reactVersion = pkg.dependencies?.react;
        const typescriptVersion = pkg.devDependencies?.typescript;
        const viteVersion = pkg.devDependencies?.vite;
        
        expect(reactVersion).toBeDefined();
        expect(typescriptVersion).toBeDefined();
        expect(viteVersion).toBeDefined();
        
        // 버전 추출
        const reactMajor = parseInt(reactVersion!.replace(/[^\d.]/g, '').split('.')[0]);
        const typescriptMajor = parseInt(typescriptVersion!.replace(/[^\d.]/g, '').split('.')[0]);
        const viteMajor = parseInt(viteVersion!.replace(/[^\d.]/g, '').split('.')[0]);
        
        // 요구사항에 맞는 최소 버전 확인
        expect(reactMajor).toBeGreaterThanOrEqual(18);
        expect(typescriptMajor).toBeGreaterThanOrEqual(5);
        expect(viteMajor).toBeGreaterThanOrEqual(5);
        
        // React 18은 TypeScript 4.7+ 필요하므로 TypeScript 5+는 호환
        // Vite 5+는 React 18과 TypeScript 5+와 호환
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('should have required state management dependencies', () => {
    fc.assert(
      fc.property(fc.constant(packageJson), (pkg) => {
        // Requirements 1.5: Zustand for client state and TanStack Query for server state
        const zustandVersion = pkg.dependencies?.zustand;
        const tanstackQueryVersion = pkg.dependencies?.['@tanstack/react-query'];
        
        expect(zustandVersion).toBeDefined();
        expect(tanstackQueryVersion).toBeDefined();
        
        // 버전이 유효한 semver 형식인지 확인
        expect(zustandVersion).toMatch(/^[\^~]?\d+\.\d+\.\d+/);
        expect(tanstackQueryVersion).toMatch(/^[\^~]?\d+\.\d+\.\d+/);
        
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('should have Shadcn/UI related dependencies', () => {
    fc.assert(
      fc.property(fc.constant(packageJson), (pkg) => {
        // Requirements 1.3: Shadcn/UI library components
        const radixDependencies = Object.keys(pkg.dependencies || {})
          .filter(dep => dep.startsWith('@radix-ui/'));
        
        // Radix UI 컴포넌트들이 있어야 함 (Shadcn/UI의 기반)
        expect(radixDependencies.length).toBeGreaterThan(0);
        
        // 필수 유틸리티들
        expect(pkg.dependencies?.['class-variance-authority']).toBeDefined();
        expect(pkg.dependencies?.clsx).toBeDefined();
        expect(pkg.dependencies?.['tailwind-merge']).toBeDefined();
        
        // Tailwind CSS
        expect(pkg.devDependencies?.tailwindcss).toBeDefined();
        
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('should have routing dependency for role-based routing', () => {
    fc.assert(
      fc.property(fc.constant(packageJson), (pkg) => {
        // Requirements 1.4: Role-based routing
        const routingLib = pkg.dependencies?.wouter;
        
        expect(routingLib).toBeDefined();
        expect(routingLib).toMatch(/^[\^~]?\d+\.\d+\.\d+/);
        
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('should have valid TypeScript configuration', () => {
    fc.assert(
      fc.property(fc.constant(true), () => {
        // TypeScript 설정 파일들이 존재하는지 확인
        const tsconfigPath = path.resolve(process.cwd(), 'tsconfig.json');
        const tsconfigAppPath = path.resolve(process.cwd(), 'tsconfig.app.json');
        
        expect(fs.existsSync(tsconfigPath)).toBe(true);
        expect(fs.existsSync(tsconfigAppPath)).toBe(true);
        
        // tsconfig.app.json 내용을 문자열로 확인 (JSON 파싱 오류 방지)
        const tsconfigAppContent = fs.readFileSync(tsconfigAppPath, 'utf-8');
        
        // React JSX 설정 확인
        expect(tsconfigAppContent).toContain('"jsx": "react-jsx"');
        
        // ES2022 타겟 확인 (React 18과 호환)
        expect(tsconfigAppContent).toContain('"target": "ES2022"');
        
        // 모듈 시스템 확인
        expect(tsconfigAppContent).toContain('"module": "ESNext"');
        expect(tsconfigAppContent).toContain('"moduleResolution": "bundler"');
        
        // 경로 별칭 설정 확인
        expect(tsconfigAppContent).toContain('"@/*"');
        
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('should have valid Vite configuration', () => {
    fc.assert(
      fc.property(fc.constant(true), () => {
        // Vite 설정 파일이 존재하는지 확인
        const viteConfigPath = path.resolve(process.cwd(), 'vite.config.ts');
        expect(fs.existsSync(viteConfigPath)).toBe(true);
        
        // Vite 설정 파일이 TypeScript로 작성되었는지 확인
        const viteConfigContent = fs.readFileSync(viteConfigPath, 'utf-8');
        
        // React 플러그인 사용 확인
        expect(viteConfigContent).toContain('@vitejs/plugin-react');
        expect(viteConfigContent).toContain('react()');
        
        // Tailwind CSS 플러그인 확인
        expect(viteConfigContent).toContain('@tailwindcss/vite');
        expect(viteConfigContent).toContain('tailwindcss()');
        
        // 경로 별칭 설정 확인
        expect(viteConfigContent).toContain('"@"');
        
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('should have production build optimization configuration', () => {
    fc.assert(
      fc.property(fc.constant(packageJson), (pkg) => {
        // Requirements 1.6: Production deployment optimization
        
        // 빌드 스크립트들이 있는지 확인
        expect(pkg.scripts?.build).toBeDefined();
        expect(pkg.scripts?.['build:production']).toBeDefined();
        
        // 프로덕션 최적화 관련 dev dependencies 확인
        expect(pkg.devDependencies?.['rollup-plugin-visualizer']).toBeDefined(); // 번들 분석
        
        // ESLint 설정 (코드 품질)
        expect(pkg.devDependencies?.eslint).toBeDefined();
        
        // 테스트 설정
        expect(pkg.devDependencies?.vitest).toBeDefined();
        expect(pkg.devDependencies?.['@testing-library/react']).toBeDefined();
        
        return true;
      }),
      { numRuns: 100 }
    );
  });

  it('should have consistent dependency versions across related packages', () => {
    fc.assert(
      fc.property(fc.constant(packageJson), (pkg) => {
        // React와 React DOM 버전 일치 확인
        const reactVersion = pkg.dependencies?.react;
        const reactDomVersion = pkg.dependencies?.['react-dom'];
        
        if (reactVersion && reactDomVersion) {
          const reactMajorMinor = reactVersion.replace(/[^\d.]/g, '').split('.').slice(0, 2).join('.');
          const reactDomMajorMinor = reactDomVersion.replace(/[^\d.]/g, '').split('.').slice(0, 2).join('.');
          expect(reactMajorMinor).toBe(reactDomMajorMinor);
        }
        
        // @types/react와 @types/react-dom 버전 일치 확인
        const typesReact = pkg.devDependencies?.['@types/react'];
        const typesReactDom = pkg.devDependencies?.['@types/react-dom'];
        
        if (typesReact && typesReactDom) {
          const typesReactMajor = parseInt(typesReact.replace(/[^\d.]/g, '').split('.')[0]);
          const typesReactDomMajor = parseInt(typesReactDom.replace(/[^\d.]/g, '').split('.')[0]);
          expect(typesReactMajor).toBe(typesReactDomMajor);
        }
        
        return true;
      }),
      { numRuns: 100 }
    );
  });
});